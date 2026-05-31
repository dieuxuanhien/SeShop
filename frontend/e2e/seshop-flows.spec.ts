import { expect, test, type Page, type Route } from '@playwright/test';

type TestUser = {
  id: number;
  username: string;
  userType: 'ADMIN' | 'STAFF' | 'CUSTOMER';
  roles: string[];
  permissions: string[];
};

const api = (path: string) => `**/api/v1${path}`;

async function fulfillData(route: Route, data: unknown, status = 200) {
  await route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify({
      data,
      meta: { traceId: 'e2e-trace' },
    }),
  });
}

async function signIn(page: Page, user: TestUser, token = `e2e-token-${user.userType.toLowerCase()}`) {
  await page.addInitScript(
    ({ token: persistedToken, user: persistedUser }) => {
      window.localStorage.setItem('seshop.accessToken', persistedToken);
      window.localStorage.setItem(
        'seshop.auth',
        JSON.stringify({
          state: {
            token: persistedToken,
            user: persistedUser,
          },
          version: 0,
        }),
      );
    },
    { token, user },
  );
}

test('customer can complete checkout with a validated discount and COD payment', async ({ page }) => {
  const customer: TestUser = {
    id: 42,
    username: 'linh.customer',
    userType: 'CUSTOMER',
    roles: ['CUSTOMER'],
    permissions: [],
  };

  await signIn(page, customer);

  await page.route(api('/carts/me'), async (route) => {
    await fulfillData(route, {
      id: 111,
      customerId: customer.id,
      status: 'ACTIVE',
      items: [
        {
          id: 1,
          variantId: 7001,
          skuCode: 'SKU-LINEN-001',
          name: 'Linen Shirt',
          qty: 2,
          unitPrice: 300000,
        },
      ],
    });
  });

  await page.route(api('/discounts/validate'), async (route) => {
    expect(route.request().postDataJSON()).toEqual({
      code: 'SAVE50',
      orderSubtotal: 600000,
    });
    await fulfillData(route, { valid: true, discountAmount: 50000 });
  });
  await page.route(api('/shipping/validate-address'), async (route) => {
    await fulfillData(route, { valid: true, message: 'Valid address' });
  });

  await page.route(api('/shipping/estimate-fee'), async (route) => {
    await fulfillData(route, { fee: 30000 });
  });


  await page.route(api('/checkout'), async (route) => {
    await fulfillData(
      route,
      {
        orderId: 9001,
        orderNumber: 'ORD-9001',
        paymentStatus: 'PENDING',
        shipmentStatus: 'PENDING',
      },
      201,
    );
  });

  await page.goto('/checkout');
  await expect(page.getByRole('heading', { name: 'Checkout' })).toBeVisible();
  await expect(page.getByText('Linen Shirt')).toBeVisible();

  await page.getByPlaceholder(/enter code/i).fill('SAVE50');
  await page.getByRole('button', { name: 'Apply' }).click();
  await expect(page.getByText('-50,000 VND')).toBeVisible();

  await page.getByLabel('Full Name').fill('Linh Nguyen');
  await page.getByLabel('Phone Number').fill('0909000000');
  await page.getByLabel('Address Line 1').fill('12 Le Loi');
  await page.getByLabel('Ward').fill('Ben Nghe');
  await page.getByLabel('District').fill('District 1');
  await page.locator('.leaflet-container').click();
  await page.getByRole('button', { name: 'Continue to Payment' }).click();
  await page.getByText('Cash on Delivery').click();

  const checkoutRequestPromise = page.waitForRequest(api('/checkout'));
  await page.getByRole('button', { name: 'Place Order' }).click();
  const checkoutRequest = await checkoutRequestPromise;

  expect(checkoutRequest.headers().authorization).toBe('Bearer e2e-token-customer');
  expect(checkoutRequest.postDataJSON()).toEqual({
    cartId: 111,
    shippingAddress: expect.objectContaining({
      fullName: 'Linh Nguyen',
      phoneNumber: '0909000000',
      line1: '12 Le Loi',
      ward: 'Ben Nghe',
      district: 'District 1',
      city: 'Ho Chi Minh City',
    }),
    paymentMethod: 'COD',
    discountCode: 'SAVE50',
  });

  await expect(page.getByRole('heading', { name: 'Order Confirmed' })).toBeVisible();
  await expect(page.getByText(/ORD-9001/)).toBeVisible();
});

test('staff can scan a SKU and complete a POS cash sale', async ({ page }) => {
  const staff: TestUser = {
    id: 7,
    username: 'staff.user',
    userType: 'STAFF',
    roles: ['STAFF'],
    permissions: ['pos.sell'],
  };

  await signIn(page, staff);

  await page.route(api('/staff/inventory/locations'), async (route) => {
    await fulfillData(route, [
      { id: 1, name: 'Main Store' }
    ]);
  });

  await page.route(api('/staff/inventory/balances/sku/SKU-LINEN-001'), async (route) => {
    await fulfillData(route, {
      variantId: 7001,
      skuCode: 'SKU-LINEN-001',
      productName: 'Linen Shirt',
      price: 250000,
    });
  });

  await page.route(api('/pos/receipts*'), async (route) => {
    if (route.request().method() === 'POST') {
      await fulfillData(route, {
        receiptId: 501,
        receiptNumber: 'RCPT-501',
        changeDue: 50000,
        totalAmount: 250000,
        paymentMethod: 'CASH',
        amountPaid: 300000,
        createdAt: new Date().toISOString(),
        locationName: 'Main Store',
        operatorName: 'Staff Member',
        items: [
          {
            id: 1,
            variantId: 7001,
            skuCode: 'SKU-LINEN-001',
            name: 'Linen Shirt',
            qty: 1,
            unitPrice: 250000,
            totalPrice: 250000
          }
        ]
      });
    } else {
      await fulfillData(route, {
        items: [],
        page: 0,
        size: 5,
        totalElements: 0,
        totalPages: 0
      });
    }
  });

  await page.goto('/staff/pos');
  await expect(page.getByRole('heading', { name: 'SeShop POS' })).toBeVisible();

  await page.getByPlaceholder(/scan barcode/i).fill('SKU-LINEN-001');
  await page.getByRole('button', { name: 'Add' }).click();
  await expect(page.getByText('Linen Shirt')).toBeVisible();
  await expect(page.getByText('250,000 x 1')).toBeVisible();

  await page.getByLabel('Amount Tendered').fill('300000');
  const receiptRequestPromise = page.waitForRequest(api('/pos/receipts'));
  await page.getByRole('button', { name: 'Complete Sale' }).click();
  const receiptRequest = await receiptRequestPromise;

  expect(receiptRequest.headers().authorization).toBe('Bearer e2e-token-staff');
  expect(receiptRequest.postDataJSON()).toEqual({
    paymentMethod: 'CASH',
    amountPaid: 300000,
    items: [
      {
        variantId: 7001,
        skuCode: 'SKU-LINEN-001',
        name: 'Linen Shirt',
        price: 250000,
        qty: 1,
      },
    ],
  });

  await expect(page.getByRole('heading', { name: 'Sale Complete' })).toBeVisible();
  await expect(page.getByText('RCPT-501', { exact: true })).toBeVisible();
  await expect(page.getByText('50,000 VND', { exact: true }).last()).toBeVisible();
});

test('staff can publish an approved Instagram draft', async ({ page }) => {
  const staff: TestUser = {
    id: 7,
    username: 'staff.user',
    userType: 'STAFF',
    roles: ['STAFF'],
    permissions: ['social.compose'],
  };

  await signIn(page, staff);

  let published = false;
  await page.route(api('/marketing/drafts'), async (route) => {
    await fulfillData(route, [
      {
        id: 99,
        productId: 501,
        caption: 'Fresh vintage drop',
        hashtags: '#seshop #newarrival',
        mediaOrder: ['https://cdn.example.com/image-1.jpg'],
        status: published ? 'PUBLISHED' : 'APPROVED',
        createdAt: '2026-05-10T12:00:00+07:00',
      },
    ]);
  });

  await page.route(api('/marketing/drafts/99/publish'), async (route) => {
    expect(route.request().method()).toBe('POST');
    published = true;
    await fulfillData(route, {
      draftId: 99,
      status: 'PUBLISHED',
      instagramCreationId: 'creation-7',
      instagramMediaId: 'media-99',
      instagramPermalink: 'https://www.instagram.com/p/media-99',
      publishedAt: '2026-05-10T12:35:00+07:00',
    });
  });

  await page.goto('/staff/marketing/drafts');
  await expect(page.getByRole('heading', { name: 'Instagram Compose & Draft Management' })).toBeVisible();
  await expect(page.getByText('Fresh vintage drop')).toBeVisible();

  await page.getByRole('button', { name: 'Edit' }).click();
  await expect(page.getByRole('button', { name: 'Publish to Instagram' })).toBeEnabled();
  const publishRequestPromise = page.waitForRequest(api('/marketing/drafts/99/publish'));
  await page.getByRole('button', { name: 'Publish to Instagram' }).click();
  await publishRequestPromise;

  await expect(page.getByText(/Draft published to Instagram/i)).toBeVisible();
});

test('unauthenticated user is redirected to login when accessing protected route', async ({ page }) => {
  await page.goto('/staff/pos');
  await expect(page).toHaveURL(/.*\/auth\/login.*/);
});

test('user without permissions sees access denied when accessing restricted route', async ({ page }) => {
  const staffNoPerms: TestUser = {
    id: 7,
    username: 'staff.noperms',
    userType: 'STAFF',
    roles: ['STAFF'],
    permissions: [],
  };
  await signIn(page, staffNoPerms);
  await page.goto('/staff/pos');
  await expect(page).toHaveURL(/.*\/access-denied.*/);
  await expect(page.getByText('Access denied')).toBeVisible();
});

test('customer cannot checkout when stock is unavailable', async ({ page }) => {
  const customer: TestUser = {
    id: 42,
    username: 'linh.customer',
    userType: 'CUSTOMER',
    roles: ['CUSTOMER'],
    permissions: [],
  };
  await signIn(page, customer);

  await page.route(api('/carts/me'), async (route) => {
    await fulfillData(route, {
      id: 111,
      customerId: customer.id,
      status: 'ACTIVE',
      items: [
        {
          id: 1,
          variantId: 7001,
          skuCode: 'SKU-LINEN-001',
          name: 'Linen Shirt',
          qty: 2,
          unitPrice: 300000,
        },
      ],
    });
  });

  await page.route(api('/shipping/validate-address'), async (route) => {
    await fulfillData(route, { valid: true, message: 'Valid address' });
  });

  await page.route(api('/shipping/estimate-fee'), async (route) => {
    await fulfillData(route, { fee: 30000 });
  });

  await page.route(api('/checkout'), async (route) => {
    await route.fulfill({
      status: 400,
      contentType: 'application/json',
      body: JSON.stringify({
        message: 'Insufficient stock for SKU-LINEN-001',
        error: 'INSUFFICIENT_STOCK',
      }),
    });
  });

  await page.goto('/checkout');
  await expect(page.getByRole('heading', { name: 'Checkout' })).toBeVisible();

  await page.getByLabel('Full Name').fill('Linh Nguyen');
  await page.getByLabel('Phone Number').fill('0909000000');
  await page.getByLabel('Address Line 1').fill('12 Le Loi');
  await page.getByLabel('Ward').fill('Ben Nghe');
  await page.getByLabel('District').fill('District 1');
  await page.locator('.leaflet-container').click();
  await page.getByRole('button', { name: 'Continue to Payment' }).click();
  await page.getByText('Cash on Delivery').click();

  await page.getByRole('button', { name: 'Place Order' }).click();

  await expect(page.getByText(/Insufficient stock/)).toBeVisible();
});

test('staff can approve a return request', async ({ page }) => {
  const staff: TestUser = {
    id: 7,
    username: 'staff.user',
    userType: 'STAFF',
    roles: ['STAFF'],
    permissions: ['refund.process'],
  };
  await signIn(page, staff);

  await page.route(api('/staff/inventory/locations'), async (route) => {
    await fulfillData(route, [
      { id: 1, name: 'Main Store' }
    ]);
  });

  await page.route(api('/staff/orders*'), async (route) => {
    await fulfillData(route, { items: [{ id: 9001, orderNumber: 'ORD-9001' }] });
  });

  await page.route(api('/staff/orders/9001*'), async (route) => {
    await fulfillData(route, { id: 9001, orderNumber: 'ORD-9001', items: [{ id: 1, productName: 'Shirt', qty: 1 }] });
  });

  await page.route(api('/staff/orders/9001/payments*'), async (route) => {
    await fulfillData(route, []);
  });

  await page.route(api('/pos/receipts*'), async (route) => {
    await fulfillData(route, { items: [] });
  });

  await page.route(api('/returns/order/9001'), async (route) => {
    await fulfillData(route, [{ returnId: 500, status: 'PENDING', reason: 'Defective' }]);
  });

  await page.route(api('/returns/500/approve'), async (route) => {
    await fulfillData(route, {
      returnId: 500,
      orderId: 9001,
      status: 'APPROVED',
      reason: 'Defective'
    });
  });

  await page.goto('/staff/returns');
  await expect(page.getByRole('heading', { name: 'Refunds & Returns' })).toBeVisible();

  await page.getByText('Search order').click();
  await page.getByText('Order ORD-9001').click();

  await page.locator('form').filter({ hasText: 'Select Pending Return' }).locator('select').selectOption('500');
  await page.getByRole('button', { name: 'Approve' }).click();
  
  await expect(page.getByText('Successfully approved return.').first()).toBeVisible();
});
