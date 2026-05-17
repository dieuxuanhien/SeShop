# Dev Setup: Instagram Publishing and GHN API

This is dev-only. No production deployment, no Meta App Review, no real customer orders.

Important Meta note: this repo's `/api/v1/webhooks/instagram/callback` endpoint is an OAuth redirect callback, not an Instagram webhook subscription callback. It normally does not need webhook verification. What can fail in dev is usually one of these:

- Redirect URI in Meta dashboard does not exactly match `META_REDIRECT_URI`.
- The app is in Development mode and your Facebook user is not an app tester/developer/admin.
- The Instagram account is not Professional.
- The Instagram account is not linked to a Facebook Page.
- The OAuth user does not manage that Page.
- Meta refuses localhost for your app settings, so you need an HTTPS tunnel.

## 1. Start The App

From repo root:

```bash
cp .env.example .env
docker compose up --build
```

Open:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

Login:

- Username: `staff.manager`
- Password: `Strong@123`

The seeded staff manager has the needed dev permissions: `social.compose`, `social.connect`, `order.read`, and `order.ship`.

## 2. Instagram Dev Setup

### Step 1: Prepare Instagram and Facebook

1. Use an Instagram Professional account, either Business or Creator.
2. Create or use a Facebook Page.
3. Link the Instagram account to that Facebook Page.
4. Make sure your Facebook user can manage that Page.
5. In the Meta Developer dashboard, add that Facebook user to the app as Admin, Developer, or Tester.

You do not need App Review if you only test with app roles/testers in development mode.

### Step 2: Configure Meta App

In Meta Developer dashboard:

1. Create/open your app.
2. Add Facebook Login.
3. Add Instagram Graph API / Instagram API capability if available in your dashboard.
4. Add this OAuth redirect URI:

```text
http://localhost:8080/api/v1/webhooks/instagram/callback
```

If Meta rejects localhost or the callback fails, use an HTTPS tunnel:

```bash
ngrok http 8080
```

Then use this style of redirect URI instead:

```text
https://your-ngrok-domain.ngrok-free.app/api/v1/webhooks/instagram/callback
```

### Step 3: Configure `.env`

Use localhost callback first:

```env
META_ENABLED=true
META_BASE_URL=https://graph.facebook.com/v25.0
META_APP_ID=your_meta_app_id
META_APP_SECRET=your_meta_app_secret
META_REDIRECT_URI=http://localhost:8080/api/v1/webhooks/instagram/callback
META_SCOPES=instagram_basic,pages_show_list,pages_read_engagement,instagram_content_publish
```

If using ngrok, replace only `META_REDIRECT_URI`:

```env
META_REDIRECT_URI=https://your-ngrok-domain.ngrok-free.app/api/v1/webhooks/instagram/callback
```

Restart backend after changing `.env`:

```bash
docker compose up --build backend
```

### Step 4: Connect From The UI

1. Login as `staff.manager`.
2. Go to `/staff/marketing/instagram`.
3. Click `Connect`.
4. Complete Facebook OAuth.
5. If the callback succeeds, the browser will show JSON. That is okay in dev.
6. Manually go back to `/staff/marketing/instagram`.
7. Click `Verify Connection`.
8. Confirm status is `CONNECTED`.

### Step 5: Publish A Test Image

1. Go to `/staff/marketing/drafts`.
2. Click `New Draft`.
3. Enter a valid product ID, for example `1`.
4. Add caption and hashtags.
5. For `Media URLs`, use a direct public HTTPS JPEG URL.
6. Click `Save Draft`.
7. Click `Submit Review`.
8. Click `Approve`.
9. Click `Publish to Instagram`.

Do not use:

- Localhost image URLs.
- LocalStack/S3 private URLs.
- Google Drive share URLs.
- WebP URLs from seed data.
- Any URL requiring login/cookies.

Use a real public `.jpg` URL. Meta fetches the image from their server.

## 3. Instagram If OAuth Callback Still Fails

Use this fallback for dev only.

### Option A: Debug the generated OAuth URL

Login to backend:

```bash
curl -s http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usernameOrEmail":"staff.manager","password":"Strong@123"}'
```

Copy `data.accessToken` into `TOKEN`, then:

```bash
curl -s -X POST http://localhost:8080/api/v1/marketing/instagram/connect \
  -H "Authorization: Bearer $TOKEN"
```

Open the returned `authorizationUrl`. Check that:

- `client_id` equals your Meta app ID.
- `redirect_uri` exactly matches Meta dashboard.
- `scope` includes `instagram_basic,pages_show_list,pages_read_engagement,instagram_content_publish`.

### Option B: Test Meta manually in Graph API Explorer

This proves your Meta account setup is valid even if the app callback is not.

1. Open Meta Graph API Explorer.
2. Select your app.
3. Generate a User Access Token with:
   - `instagram_basic`
   - `pages_show_list`
   - `pages_read_engagement`
   - `instagram_content_publish`
4. Call:

```text
GET /me/accounts?fields=id,name,access_token,instagram_business_account{id,username}
```

5. Confirm one Page returns `instagram_business_account.id`.
6. Use that IG ID and Page access token to create a media container:

```text
POST /{ig-user-id}/media?image_url=https://example.com/test.jpg&caption=SeShop dev test&access_token={page-access-token}
```

7. Publish the returned container ID:

```text
POST /{ig-user-id}/media_publish?creation_id={container-id}&access_token={page-access-token}
```

If this works but SeShop callback does not, your Meta account setup is okay and the problem is the app redirect/localhost/tunnel configuration.

## 4. GHN Dev Setup

### Step 1: Get GHN Dev Credentials

Use GHN's dev/test gateway credentials:

- API token
- Shop ID

Use only:

```text
https://dev-online-gateway.ghn.vn
```

### Step 2: Configure `.env`

```env
GHN_ENABLED=true
GHN_BASE_URL=https://dev-online-gateway.ghn.vn
GHN_TOKEN=your_ghn_dev_token
GHN_SHOP_ID=your_ghn_dev_shop_id
GHN_CREATE_ORDER_PATH=/shiip/public-api/v2/shipping-order/create
GHN_TRACK_PATH=/shiip/public-api/v2/shipping-order/detail
```

Restart backend:

```bash
docker compose up --build backend
```

### Step 3: Understand Current GHN Limitation

The backend calls GHN only when shipping request has:

- `carrier: "GHN"`
- no `trackingNumber`
- `recipientName`
- `recipientPhone`
- existing order shipping address

Current frontend does not do this. It sends a fake tracking number:

```ts
shipOrder(order.id, `TRACK-${order.id}`)
```

That means backend treats it as manual shipping and skips GHN.

For GHN dev testing, call the API directly.

### Step 4: Ship Through GHN By API

Login:

```bash
curl -s http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usernameOrEmail":"staff.manager","password":"Strong@123"}'
```

Copy `data.accessToken` into `TOKEN`.

Find an order in staff UI or API, then make sure it is shippable. It must be paid or COD pending, and status should be `CONFIRMED`, `PAID`, `ALLOCATED`, or `PACKED`.

You can use staff UI to allocate and pack first, or call:

```bash
curl -s -X POST http://localhost:8080/api/v1/staff/orders/1/allocate \
  -H "Authorization: Bearer $TOKEN"

curl -s -X POST http://localhost:8080/api/v1/staff/orders/1/pack \
  -H "Authorization: Bearer $TOKEN"
```

Now ship with GHN. Do not send `trackingNumber`:

```bash
curl -s -X POST http://localhost:8080/api/v1/staff/orders/1/ship \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "carrier": "GHN",
    "recipientName": "Nguyen Van A",
    "recipientPhone": "0987654321"
  }'
```

If GHN returns an order code, SeShop stores it as shipment tracking number.

### Step 5: Track GHN Shipment

```bash
curl -s -X POST http://localhost:8080/api/v1/orders/1/track-shipment \
  -H "Authorization: Bearer $TOKEN"
```

## 5. GHN If Create Order Fails

The current repo sends a minimal GHN body:

```json
{
  "client_order_code": "ORD-...",
  "to_name": "...",
  "to_phone": "...",
  "to_address": "...",
  "required_note": "KHONGCHOXEMHANG"
}
```

GHN dev may reject this because real create-order usually needs more fields:

- `payment_type_id`
- `service_type_id`
- `to_ward_code` or `to_ward_name`
- `to_district_id` or district/province names
- `weight`
- `length`
- `width`
- `height`
- `items`
- optional `cod_amount`
- optional `insurance_value`

For fastest dev success, update `GhnClient.createShippingOrder()` later to include default parcel fields and address codes. Until then, a GHN error does not mean credentials are wrong.

## 6. Dev Checklist

Instagram:

- `.env` has `META_ENABLED=true`.
- Meta app has the exact redirect URI.
- Facebook user is app tester/developer/admin.
- Instagram account is Professional.
- Instagram account is linked to a Page.
- OAuth user manages the Page.
- `/staff/marketing/instagram` reaches `CONNECTED`.
- Draft media URL is a public HTTPS JPEG.

GHN:

- `.env` has `GHN_ENABLED=true`.
- GHN base URL is the dev gateway.
- Token and Shop ID are dev credentials.
- Ship request does not include `trackingNumber`.
- Ship request includes `recipientName` and `recipientPhone`.
- If GHN rejects create-order, add required parcel/address fields to `GhnClient`.

## 7. Sources

- Meta Instagram API Postman collection: https://www.postman.com/meta/workspace/instagram/documentation/23987686-9386f468-7714-490f-9bfc-9442db5c8f00
- GHN create-order docs: https://api.ghn.vn/home/docs/detail?id=123
- GHN order-info docs: https://api.ghn.vn/home/docs/detail?id=119
