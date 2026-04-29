# SeShop - Views Descriptions

**Project:** SeShop - Omnichannel Clothing & Accessories Platform

**Date:** 2026-03-27

**Version:** 1.0

---

**Scope note (v1):** This document includes a few future-facing UX ideas. Any feature marked **Future (v2)** is not required for the initial implementation and is out of scope for the v1 BRD/SRS.

## Table of Contents

1. [Admin Views](#admin-views)
2. [Staff Views](#staff-views)
3. [Customer Views](#customer-views)

---

## Admin Views

### View 01: Dashboard Overview

| Attribute | Value |
|-----------|-------|
| **View Name** | Dashboard Overview |
| **View Code** | ADMIN_001 |
| **Purpose** | Provide Super Admin with real-time business metrics and system health overview |
| **Target Users** | Super Admin |
| **Data Display** | Key performance indicators, alerts, system status |
| **Components** | Widget cards (revenue, orders, inventory alerts), charts (sales trend, top products), quick action buttons |
| **Key Metrics** | Total revenue (today/week/month), Active orders, Low stock items, Staff online count, System health status |
| **Interaction** | Date range picker, metric drill-down, export reports |
| **Refresh Cycle** | Real-time updates for critical metrics, 5-minute refresh for general data |

### View 02: User & Role Management

| Attribute | Value |
|-----------|-------|
| **View Name** | User & Role Management Dashboard |
| **View Code** | ADMIN_002 |
| **Purpose** | Manage staff accounts, create/edit custom roles, and assign permissions granularly |
| **Target Users** | Super Admin |
| **Data Display** | List of staff members, roles, permissions matrix, activity logs |
| **Components** | Staff table (name, location, role, status), role creation form, permission selector modal, audit trail |
| **Key Features** | Create/edit custom roles, assign multi-role to staff, granular permission control, role preview, import/export staff list |
| **Actions** | Add staff, edit staff details, assign roles, create role, modify permissions, deactivate user, view role audit |
| **Search & Filter** | Filter by location, role, status; search by name/email |
| **Workflow** | Select staff → Assign role → Configure permissions → Save & confirm |

### View 03: Locations Management

| Attribute | Value |
|-----------|-------|
| **View Name** | Locations & Inventory Hubs Management |
| **View Code** | ADMIN_003 |
| **Purpose** | Configure and monitor all store and storage locations for inventory operations |
| **Target Users** | Super Admin, Inventory Manager |
| **Data Display** | Location list with type (STORE/STORAGE), address, contact, operating hours, inventory stats |
| **Components** | Location grid/map view toggle, location detail card, inventory heatmap by location, staff assigned per location |
| **Key Metrics** | Total SKU by location, low stock alerts per location, transfer queue by location |
| **Actions** | Add location, edit location info, deactivate location, view inventory snapshot, assign staff to location |
| **Search & Filter** | Filter by location type, region, status; search by name/code |

### View 04: Audit & Compliance Logs

| Attribute | Value |
|-----------|-------|
| **View Name** | Audit & Compliance Logs Viewer |
| **View Code** | ADMIN_004 |
| **Purpose** | Track all system operations for compliance and troubleshooting |
| **Target Users** | Super Admin, Compliance Officer |
| **Data Display** | Timestamped logs of all user actions (role changes, inventory adjustments, refunds, POS transactions) |
| **Components** | Advanced log table with filters, timeline view option, export function, real-time alert panel |
| **Log Categories** | User management, inventory changes, order operations, POS transactions, refunds, transfers, payments, role changes |
| **Metadata per Log** | Timestamp, user ID, action type, affected resource ID, before/after values, IP address, status |
| **Actions** | Filter by date range, user, action type, location; export CSV/PDF; generate compliance report |
| **Search & Filter** | Multi-level filtering (date, user, action, location, status); full-text search |

### View 05: System Configuration

| Attribute | Value |
|-----------|-------|
| **View Name** | System Configuration & Settings |
| **View Code** | ADMIN_005 |
| **Purpose** | Configure global system settings, payment methods, and integrations |
| **Target Users** | Super Admin, Technical Admin |
| **Data Display** | Configuration sections for business info, payment gateways, shipping providers, Instagram integration, email settings |
| **Components** | Settings form groups (tabbed interface), credential manager, API key viewer (masked), test connection button, toggle switches |
| **Key Settings** | Business name/logo, payment methods (Stripe, CoD), shipping rules, Instagram credentials/webhooks, email templates, currency/timezone |
| **Actions** | Update settings, test payment gateway, regenerate API keys, verify Instagram connection, preview email templates |
| **Workflow** | Select setting category → Modify values → Test configuration → Save & confirm |

---

## Staff Views

### View 06: Catalog Management (Product & SKU)

| Attribute | Value |
|-----------|-------|
| **View Name** | Catalog Management - Products & SKU |
| **View Code** | STAFF_001 |
| **Purpose** | Create, edit, and manage product catalog with variant SKUs (size, color combinations) |
| **Target Users** | Catalog Manager, Product Owner |
| **Permissions Required** | `PROD:CREATE`, `PROD:EDIT`, `PROD:VIEW`, `SKU:MANAGE` |
| **Data Display** | Product list with thumbnail, SKU variants table, pricing, categories, tags |
| **Components** | Product grid/list view toggle, product form (name, description, category, media), SKU matrix editor, bulk SKU actions |
| **Key Features** | Add product, edit product details, bulk upload SKU from CSV, manage SKU combinations (size×color), set pricing per SKU, reorder priority |
| **SKU Fields** | SKU code, size, color, weight, cost price, selling price, barcode, reorder level, supplier info |
| **Actions** | Create product, bulk import SKU, edit SKU pricing, archive product/SKU, duplicate product, view stock across locations |
| **Media Handling** | Upload product images (thumbnail, gallery), organize into collections, crop/resize preview |
| **Search & Filter** | Search by product name/SKU code, filter by category, supplier, stock status; sort by creation date, popularity, price |
| **Rich Editor** | Product description with rich text (bold, links, bullet points), Instagram-ready preview panel |

### View 07: Inventory Adjustment & Movement

| Attribute | Value |
|-----------|-------|
| **View Name** | Inventory Adjustment & Stock Movement |
| **View Code** | STAFF_002 |
| **Purpose** | Adjust stock levels, record inventory discrepancies, and manage stock movements between locations |
| **Target Users** | Warehouse Staff, Inventory Clerk, Logistics Manager |
| **Permissions Required** | `INV:ADJUST`, `INV:TRANSFER`, `INV:VIEW` |
| **Data Display** | Current stock levels by location, adjustment history, transfer queue, low stock alerts |
| **Components** | Stock adjustment form (SKU search, current qty, adjustment qty, reason, notes), transfer order creation form, location picker |
| **Adjustment Types** | Physical count discrepancy, damage/loss, return intake, stock correction, sample use |
| **Transfer Types** | Store-to-store, store-to-storage, storage-to-store, receiving from supplier |
| **Actions** | Record inventory adjustment, create transfer order, approve transfer, receive stock, cancel transfer, revert adjustment |
| **Validation** | Check available stock before transfer, confirm transfer with photos, barcode verification option |
| **Workflow** | Select SKU → Enter quantity → Choose reason/transfer type → Add notes → Capture photo → Submit for approval |
| **Approval Flow** | Submitted by warehouse staff → Pending approval (if threshold exceeded) → Approved by supervisor → Update system |

### View 08: Stock Transfer & Logistics

| Attribute | Value |
|-----------|-------|
| **View Name** | Stock Transfer & Logistics Management |
| **View Code** | STAFF_003 |
| **Purpose** | Initiate, track, approve, and receive inter-location stock transfers |
| **Target Users** | Logistics Manager, Warehouse Supervisor, Location Staff |
| **Permissions Required** | `INV:TRANSFER`, `TRANSFER:APPROVE`, `TRANSFER:RECEIVE`, `INV:VIEW` |
| **Data Display** | Transfer orders list (status, origin, destination, items, created date), transfer tracking map/timeline |
| **Status States** | DRAFT, IN_TRANSIT, COMPLETED, CANCELLED |
| **Components** | Transfer order list with status badges, transfer detail card, shipment tracking panel, receiving confirmation form |
| **Key Fields** | ID, from location, to location, items (SKU, qty, expected receive date), initiated date, expected delivery date, notes |
| **Actions** | Create transfer, mark in transit, confirm receipt, update tracking, receive stock, cancel transfer |
| **Receiving Workflow** | Scan transfer ID → Verify item list → Scan items/barcodes → Note discrepancies → Confirm receipt with signature/photo |
| **Notifications** | Auto-notify destination location when shipment in transit, reminder for delayed transfers |
| **Reports** | Transfer history by origin/destination, transfer delay analysis, transfer efficiency metrics |
| **Search & Filter** | Filter by status, date range, origin/destination location; search by transfer ID or SKU |

### View 09: Orders Management (Online & POS)

| Attribute | Value |
|-----------|-------|
| **View Name** | Orders Management Center |
| **View Code** | STAFF_004 |
| **Purpose** | View, manage, and fulfill online and POS orders; allocate orders to fulfillment location |
| **Target Users** | Order Fulfillment Staff, Sales Manager, Location Manager |
| **Permissions Required** | `ORDER:VIEW`, `ORDER:MANAGE`, `ORDER:ALLOCATE`, `FULFILLMENT:MANAGE` |
| **Data Display** | Orders list (order ID, customer, channel, date, status, total), order detail panel with timeline |
| **Order Channels** | Online (E-commerce), POS (In-store), Order allocation status |
| **Status States** | PENDING, ALLOCATED, PICKING, PACKED, SHIPPED, DELIVERED, COMPLETED, CANCELLED, RETURNED |
| **Components** | Order list with filters/sort, order detail modal, fulfillment location assignment panel, shipment tracking widget |
| **Key Actions** | View order details, print order/invoice, mark as packed, generate shipping label, allocate to location |
| **Allocation Logic** | System suggests closest location with stock → Staff confirms → System reserves inventory at that location |
| **Fulfillment Workflow** | PENDING → Assign location → PICKING (in warehouse) → PACKED → Scan tracking → SHIPPED → Customer tracking |
| **Labels & Documents** | Print pick list, packing slip, shipping label, invoice, gift message |
| **Notifications** | Auto-notify customer when shipped with tracking link, alert staff of delayed fulfillment |
| **Search & Filter** | Filter by channel, status, date range, location; search by order ID, customer name, SKU |
| **Bulk Actions** | Bulk print labels, bulk generate shipping labels, bulk mark as packed |

### View 10: Refunds & Returns Management

| Attribute | Value |
|-----------|-------|
| **View Name** | Refunds & Returns Processing |
| **View Code** | STAFF_005 |
| **Purpose** | Process customer refunds, handle returns intake, and manage exchanges |
| **Target Users** | Return Clerk, Customer Service Staff, Finance Officer |
| **Permissions Required** | `REFUND:PROCESS`, `RETURN:INTAKE`, `ORDER:MANAGE` |
| **Data Display** | Return/refund request list, return status, refund amount, payment method, return reason |
| **Return Processing Steps** | Customer initiates return → Generate return label → Return received → Inspection → Approval → Refund processed |
| **Refund Methods** | Original payment method, exchange for another item |
| **Components** | Return request list, intake form (item inspection, condition, damage assessment), payment processing panel, exchange selector |
| **Intake Workflow** | Scan return barcode → Inspect item (photos) → Verify condition (new/used/damaged) → Document findings → Confirm receipt |
| **Return Status** | REQUESTED, LABEL_GENERATED, RETURNED, RECEIVED, INSPECTED, APPROVED, REJECTED, CLOSED |
| **Refund Status** | PENDING, APPROVED, PROCESSING, COMPLETED, REJECTED |
| **Actions** | Generate return label, receive return, inspect item, approve/reject refund, process refund, create exchange order |
| **Business Rules** | Return window: 30 days from delivery, condition check required, refund timeline: 5-7 business days |
| **Analytics** | Return rate by product, common return reasons, refund processing time metrics, return fraud detection |
| **Search & Filter** | Filter by status, reason, date range, payment method; search by order ID, SKU |

### View 11: Discount & Promotion Management

| Attribute | Value |
|-----------|-------|
| **View Name** | Discount Codes & Promotion Manager |
| **View Code** | STAFF_006 |
| **Purpose** | Create and manage discount codes, promotional campaigns, and pricing rules |
| **Target Users** | Marketing Manager, Sales Manager, Promotions Coordinator |
| **Permissions Required** | `PROMO:CREATE`, `PROMO:MANAGE`, `DISCOUNT:VIEW` |
| **Data Display** | Active/expired discount codes, promotion list, usage statistics, redemption analytics |
| **Discount Types** | Fixed amount, percentage |
| **Components** | Discount creation form, coupon list table, promotion calendar, usage analytics dashboard |
| **Key Fields** | Code, discount type, value, min purchase amount, max redemptions, start/end date |
| **Creation Workflow** | Define discount type → Set value & conditions → Set date range → Add usage limits → Preview → Create |
| **Advanced Features** | Schedule discount for future date, set maximum redemption count, set minimum spend |
| **Actions** | Create discount, activate/deactivate, edit discount terms, clone discount, track usage in real-time |
| **Analytics** | Redemption rate, revenue impact, popular discount codes |
| **Notifications** | Alert when discount near redemption limit, completion notifications, anomaly detection (fraud attempts) |
| **Search & Filter** | Filter by status, date range, discount type; search by code; sort by creation date, redemptions, revenue impact |
| **Compliance** | Discount audit trail, creator/modifier tracking, approval workflow for high-value promotions |

### View 12: POS (Point of Sale) Transaction

| Attribute | Value |
|-----------|-------|
| **View Name** | Point of Sale (POS) Terminal |
| **View Code** | STAFF_007 |
| **Purpose** | Process in-store customer transactions, apply discounts, handle payments, and print receipts |
| **Target Users** | Cashier, Sales Associate, Location Manager |
| **Permissions Required** | `POS:OPERATE`, `PAYMENT:PROCESS`, `REFUND:PROCESS` |
| **Display Type** | Full-screen touch interface optimized for retail counter operations |
| **Main Components** | Product search/barcode scanner, shopping basket, payment method selector, receipt printer, cash drawer integration |
| **Transaction Workflow** | Scan items → Verify qty/price → Apply discount code → Select payment method → Process payment → Print receipt → Complete |
| **Search Methods** | Barcode scan, product name search, SKU search, category browse; quick access to best-sellers |
| **Shopping Basket** | Item list (product, SKU, qty, price), subtotal, discount applied, tax calculation, grand total |
| **Discount Application** | Enter discount code, system validates and applies, shows discount amount and new total |
| **Payment Methods** | Card (Stripe), Cash |
| **Payment Processing** | Take cash or card payment → Verify amount → Capture authorization when needed → Receipt selection → Completion, integrated receipt printer |
| **Receipt Options** | Print receipt, email receipt (if customer email provided), SMS receipt, digital receipt via QR code |
| **Shift Management** | Morning cash count → Transactions throughout day → End of shift summary → Next shift verification |
| **Actions** | Void transaction (before payment), process refund (from receipt), apply discount, reprint receipt, switch to another staff |
| **Security** | PIN required for void/refund actions, transaction encrypted, audit log of all POS operations |

### View 13: POS Shift Close & Cash Reconciliation

| Attribute | Value |
|-----------|-------|
| **View Name** | POS Shift Close & Cash Reconciliation |
| **View Code** | STAFF_008 |
| **Purpose** | Close daily shift, reconcile cash box, verify payment totals, generate shift reports |
| **Target Users** | Cashier, Supervisor, Location Manager |
| **Permissions Required** | `POS:OPERATE`, `SHIFT:CLOSE`, `FINANCE:VIEW` |
| **Components** | Shift summary card, transaction list, cash count form, payment method breakdown, discrepancy alerts, print report button |
| **Shift Summary Data** | Shift ID, cashier name, start time, end time, transaction count, gross sales, discounts applied, refunds, payments received |
| **Payment Breakdown** | Cash total, card total |
| **Cash Reconciliation** | System calculated total (from transactions) vs. physical cash count → Auto-flag discrepancy if variance beyond threshold |
| **Workflow** | Count physical cash → Enter cash count amount → System compares with calculated total → Review discrepancy → Manager approval → Close shift |
| **Discrepancy Handling** | Document discrepancy reason (if exists), supervisor review, correction adjustment, note findings, additional verification if needed |
| **Reports** | Shift summary report (printable), daily sales summary, payment method breakdown, top-selling items, average transaction value |
| **Actions** | Enter cash count, submit for approval, recount (if discrepancy), cancel discrepancy, view transaction details, print shift report, email report |
| **Approval Workflow** | Cashier submits → Variance check → Auto-approve if within threshold → Manual approval by supervisor if exceeds threshold |
| **Notifications** | Alert if large discrepancy detected, summary email to location manager with key metrics |

### View 14: Instagram Compose & Draft Management

| Attribute | Value |
|-----------|-------|
| **View Name** | Instagram Compose & Draft Publication Manager |
| **View Code** | STAFF_009 |
| **Purpose** | Create, edit, and draft Instagram posts with product recommendations and assets for manual publication |
| **Target Users** | Social Media Manager, Content Creator, Marketing Coordinator |
| **Permissions Required** | `SOCIAL:COMPOSE`, `SOCIAL:DRAFT`, `SOCIAL:PUBLISH_APPROVAL` |
| **Data Display** | Draft gallery (thumbnails), current draft revisions, product recommendation library, asset gallery |
| **Compose Interface** | Rich text editor (caption with emojis/hashtags), image upload/gallery selector, product tag selector |
| **Components** | Image editor (crop/filter/text overlay), caption editor, product selector panel, audience/location targeting options, preview panel |
| **Image Features** | Drag-drop upload, multiple images (carousel), Instagram-ready templates, built-in filters, text overlay, brand asset insertion |
| **Caption Editor** | Hashtag suggestions (trending, brand-relevant), emoji picker, mention suggestions, character counter (2200 limit) |
| **Product Tagging** | Search & select products by SKU/name, show product image thumbnail in draft, link to customer storefront |
| **Advanced Options** | Add call-to-action button (link to storefront/product), location tag, collaborator mentions |
| **Draft States** | NEW, EDITING, READY_FOR_REVIEW, APPROVED, ARCHIVED |
| **Review & Approval** | Manager reviews draft (preview full post) → Approve → hand off for manual publish |
| **Actions** | Create draft, edit draft, preview (mobile/desktop view), submit for approval, approve/reject, publish manually (with account), archive |
| **Asset Library** | Browse brand photos, product images, past successful posts, trending designs, reuse/customize assets |
| **Analytics** | Track published post performance (likes, comments, clicks) if Instagram connected, AI suggestions on trending formats |
| **Search & Filter** | Filter drafts by status, date range, product category; search by caption keywords; sort by creation date, last edit |
| **Bulk Actions** | Bulk delete archived drafts, bulk update captions, bulk revert to draft status |

### View 15: Instagram Account Connectivity & Settings

| Attribute | Value |
|-----------|-------|
| **View Name** | Instagram Account Connection & Configuration |
| **View Code** | STAFF_010 |
| **Purpose** | Connect/reconnect Instagram business account, manage API settings, view connected status and permissions |
| **Target Users** | Social Media Manager, Admin, Technical Admin |
| **Permissions Required** | `SOCIAL:CONFIG`, `SOCIAL:CONNECT` |
| **Components** | Connection status indicator, Instagram account info card, permissions display, token expiry warning, reconnect button |
| **Connection Details** | Instagram business account ID, account name, profile picture, followers count (if available), connection date |
| **OAuth Flow** | Click "Connect Instagram" → Redirect to Instagram OAuth → User authorizes SeShop app → Token saved securely → Return to app with confirmation |
| **Permissions Granted** | Access to view analytics and manage content connection (scopes validated for the connected business account) |
| **Token Management** | Display token expiry date, auto-refresh capability, manual refresh option, disconnect account option |
| **Settings** | Default business account selection, notification preferences (new follower alerts, message alerts) |
| **Status Indicators** | Connected/Disconnected, token valid/expired, last sync time, sync frequency |
| **Actions** | Connect account (OAuth), disconnect account, verify connection, manually refresh token, view account permissions, export analytics data |
| **Troubleshooting** | Display common issues (token expired, permissions revoked) with resolution guidance, manual reconnect link |
| **Notifications** | Alert when token about to expire (7 days before), warning when connection lost, confirmation when account reconnected |
| **Logs** | Connection history (who, when), disconnection history, token refresh attempts, permission changes |

---

## Customer Views

### View 16: Homepage & Product Browsing

| Attribute | Value |
|-----------|-------|
| **View Name** | Homepage & Product Browsing Interface |
| **View Code** | CUST_001 |
| **Purpose** | Welcome customers, showcase featured products, categories, and enable product discovery |
| **Target Users** | All Customers (Guest & Logged-in) |
| **Display Type** | Responsive web & mobile interface |
| **Main Components** | Hero banner (seasonal campaigns), featured products carousel, category grid, search bar, shopping cart button, user account menu |
| **Hero Banner** | Featured campaign with large imagery, headline, CTA button; rotates through current promotions |
| **Featured Products** | Dynamic carousel showing trending/new/discount products with thumbnail, name, price, star rating, quick view button |
| **Category Grid** | Visual tiles for each category (Clothing, Accessories, etc.) with category image, label, item count |
| **Navigation** | Top menu (Home, Shop, About, Contact), breadcrumb trail, category sidebar, search/filter area |
| **Search & Filter** | Search bar (autocomplete suggestions), filter by category, price range, size, color, brand, rating, availability, sort (popularity, newest, price) |
| **Quick Actions** | Add to wishlist icon, quick view modal, add to cart button visible on hover |
| **Responsive Design** | Mobile-optimized layout, touch-friendly buttons, vertical scrolling for mobile, bottom navigation bar on mobile |
| **Performance** | Fast load times, lazy-loaded images, optimized for slow network connections |
| **Engagement** | Recently viewed products shelf, personalized recommendations (if logged in), new arrival badges, stock availability indicator |

### View 17: Product Detail & Variant Selection

| Attribute | Value |
|-----------|-------|
| **View Name** | Product Detail & Variant Selection Page |
| **View Code** | CUST_002 |
| **Purpose** | Display comprehensive product information, showcase variants (size/color), and enable quick add-to-cart |
| **Target Users** | All Customers (Guest & Logged-in) |
| **Page Sections** | Product images gallery (left), product info panel (right), variant selector, quantity selector, action buttons |
| **Product Images** | Large primary image, thumbnail gallery, zoom functionality (hover for desktop, pinch for mobile), 360° view if available |
| **Product Info** | Product name, brand, SKU code, star rating with review count, price, sale badge (if on discount), description |
| **Variant Selector** | Show available sizes/colors as clickable buttons or dropdown, crossed-out unavailable options, visual color swatches |
| **Stock Availability** | "In Stock" / "Out of Stock" status, show available at specific locations if logged in, estimated restock date if applicable |
| **Quantity Selector** | Number input or +/- buttons, max quantity selector, stock limit enforcement (can't order more than available) |
| **Action Buttons** | "Add to Cart" (prominent), "Add to Wishlist" (heart icon), "Share" options (social, email, SMS) |
| **Info Tabs** | Description, specifications/materials, shipping info, size guide, return policy, customer reviews |
| **Details Tab Content** | Full product description, care instructions, materials, dimensions, weight, suggested complementary items |
| **Size Guide** | Interactive size chart (sizing by brand if applicable), fit description (slim/regular/loose), customer size reviews |
| **Shipping Info** | Estimated delivery time (based on selected location), shipping cost estimate |
| **Customer Reviews Section** | Star rating distribution, review list (rating, reviewer name, date, review text), review photos |
| **Recommendations** | "Frequently Bought Together" section, "Similar Items" carousel, "Customers Also Viewed" section |
| **Location Availability** | If customer provided location, show "Available at Store XYZ nearby" with address and distance |
| **Notifications** | "Notify me" button if out of stock, email/SMS notification when back in stock |
| **Wishlist** | Toggle wishlist status, show if already in customer's wishlist |

### View 18: Stock Availability by Location

| Attribute | Value |
|-----------|-------|
| **View Name** | Stock Availability Locator by Location |
| **View Code** | CUST_003 |
| **Purpose** | Show customers stock levels at nearby physical locations for visit planning |
| **Target Users** | All Customers (Guest & Logged-in) |
| **Display Type** | Modal dialog or dedicated page, map + list view options |
| **Map View** | Interactive map showing store locations, markers indicate stock status (green=in stock, yellow=low, red=out), click marker for details |
| **List View** | Ordered by proximity to customer location, show: store name, address, distance, phone, hours, stock quantity for selected variant |
| **Search Source** | Auto-detect customer location (if permission granted) OR allow manual location/zip code entry OR show all locations with distance calculation |
| **Location Details** | Store hours (today/week), phone number, address with Google Maps link, parking info, staff availability (if staffed location) |
| **Stock Info** | Quantity available for selected SKU at that location, last updated timestamp |
| **Actions** | Call store, get directions (Google Maps) |
| **Filtering** | Show only in-stock locations, within X miles, open now |
| **Mobile Optimized** | Location map on mobile tailored for small screens, swipe between map/list, prominent call/directions buttons |

### View 19: Shopping Cart

| Attribute | Value |
|-----------|-------|
| **View Name** | Shopping Cart Page |
| **View Code** | CUST_004 |
| **Purpose** | Review selected items, adjust quantities, manage wishlist conversion, and proceed to checkout |
| **Target Users** | All Customers (Guest & Logged-in) |
| **Cart Display** | Item list showing: product image, name, selected variant (size/color), quantity, unit price, line total, remove button |
| **Cart Items** | For each item: SKU code, variant details, in-stock indicator, update quantity (via +/- buttons or input), remove item, move to wishlist |
| **Summary Section** | Subtotal, tax (auto-calculated based on location), shipping cost, discount code input, promo code savings display |
| **Discount Application** | Text input for discount code, apply button, shows discount details if valid (amount/percentage saved) |
| **Promo Code Validation** | Real-time validation, error message if code invalid/expired, applicable items highlighted if category-specific |
| **Stock Validation** | Alert if item goes out of stock, suggest alternative size/color, option to remove or keep in cart waiting for restock |
| **Shipping Options** | Flat rate, estimated delivery date based on current date |
| **Recommendations** | "You might also like" section with up-sell/cross-sell product suggestions |
| **Checkout Button** | "Proceed to Checkout" (prominent call-to-action), "Continue Shopping" link |
| **Order Summary** | Desktop sidebar OR mobile expandable: itemized list, running total, grand total |
| **Notifications** | Auto-save cart (persists across sessions), abandoned cart recovery suggestion |
| **Guest Checkout Option** | "Continue as Guest" OR "Sign In to See Your Saved Items" |
| **Empty Cart State** | Friendly message encouraging browsing, link to shop, suggested category tiles |

### View 20: Checkout & Payment

| Attribute | Value |
|-----------|-------|
| **View Name** | Checkout & Payment Processing |
| **View Code** | CUST_005 |
| **Purpose** | Collect shipping & billing info, confirm order, and process payment securely |
| **Target Users** | All Customers (Guest & Logged-in) |
| **Checkout Steps** | Multi-step form: (1) Shipping Info, (2) Billing Info, (3) Payment Method, (4) Order Review, (5) Confirmation |
| **Step 1: Shipping Info** | Full name, phone, street address, city, state/province, ZIP/postal code, country, save address for future orders |
| **Step 2: Billing Info** | Same as shipping (checkbox toggle) OR different (uncommon), full address required |
| **Step 3: Payment Method** | Dropdown selector: Credit/Debit Card (Stripe), Cash on Delivery |
| **Payment Form** | Card number, expiry date, CVV, cardholder name (auto-filled if available), save card checkbox (for future orders) |
| **Payment Processing** | Secure Stripe integration, PCI-compliant, encrypted transmission, real-time validation of card details |
| **Step 4: Order Review** | Summary of items, shipping address, billing address, payment method, order total |
| **Step 5: Confirmation** | Order submitted message, order ID displayed, estimated delivery date, next steps (tracking, customer service contact) |
| **Form Validation** | Real-time field validation (email format, phone format, postal code format), required field indicators, error messages inline |
| **Security Indicators** | SSL padlock icon, security badges, payment provider logos, mobile-safe payment methods clearly indicated |
| **Promo Code on Checkout** | Re-entry of discount code (if not already applied in cart), final discount confirmation before payment |
| **Tax Calculation** | Auto-calculate state/provincial tax based on shipping address, display tax breakdown |
| **Estimated Delivery** | Show estimated delivery date (based on current stock and shipping time), option to expedite (if available) |
| **Order Confirmation Email** | Send confirmation email with order details, tracking info, customer service contact, return policy link |
| **CoD Workflow** | For Cash on Delivery: skip payment form, show "Payment Due on Delivery" message, payment collect at address |
| **Error Handling** | Retry logic for declined cards, clear error messages with suggested fix (e.g., "Card declined, try different card"), support contact link |
| **Mobile Optimization** | Single-column layout, large touch fields, mobile-friendly keyboard, progress bar showing checkout step |
| **Return to Cart** | "Back to Cart" link (but warn of data loss if backing out of checkout without completing) |
| **Accessibility** | WCAG AA compliant, screen reader friendly, keyboard navigation support, clear focus indicators |

### View 21: Order Tracking & Shipment Status

| Attribute | Value |
|-----------|-------|
| **View Name** | Order Tracking & Shipment Status Dashboard |
| **View Code** | CUST_006 |
| **Purpose** | Allow customers to track order status, view shipment details, and monitor delivery progress |
| **Target Users** | Logged-in Customers |
| **Tracking Entry** | Order History list → Click order → View tracking detail page |
| **Order Summary** | Order ID, order date, total amount, payment status (paid/pending), delivery address |
| **Status Timeline** | Visual timeline showing: Confirmed → Packed → Shipped → In Transit → Delivered, with timestamps and location info |
| **Current Status** | Large display of current status, timestamp of last update, next expected status |
| **Tracking Number** | Display tracking number (with carrier logo), hyperlink to carrier's tracking url for detailed tracking |
| **Carrier Info** | Shipping carrier name (e.g., FedEx, UPS, Local Courier), estimated delivery date, real-time tracking (if available) |
| **Location Tracking** | Last known location of shipment, ETA for delivery window (if available), map view of shipment path (if supported) |
| **Shipment Items** | List of items in shipment, SKU, quantity, verify matches order |
| **Notifications** | Email + SMS sent at each status change (shipped, out for delivery, delivered), allow notification preferences customization |
| **Delivery Details** | Expected delivery date/window, delivery address, special delivery instructions (leave at door, signature required) |
| **Contact Options** | "Contact Support" button, click to initiate chat/support ticket, frequently asked questions about tracking |
| **Live Chat** | Chat button available if order not delivered, connect with customer service agent for tracking questions |
| **Return Initiation** | "Return Item" button (if within return window), initiate return process with return label generation |
| **Proof of Delivery** | Once delivered, show delivery photo/signature (if available), allow dispute if claim of non-delivery |
| **Mobile Optimize** | Full-screen status timeline, large status badges, easy carrier tracking link, prominent contact support button |
| **Multi-item Orders** | If multiple shipments, show each shipment with separate timeline, allow tracking each shipment independently |

### View 22: Product Reviews & Ratings

| Attribute | Value |
|-----------|-------|
| **View Name** | Product Reviews & Rating Management |
| **View Code** | CUST_007 |
| **Purpose** | Allow customers to leave product reviews with images, view reviews, and help purchasing decisions |
| **Target Users** | All Customers (Logged-in to submit reviews) |
| **Review Submission** | Available post-purchase (order marked delivered) or triggered via email/SMS, one review per purchased item normally |
| **Review Form** | Star rating (1-5, visual stars), review text, optional image upload |
| **Star Rating** | Clickable stars, hover shows rating labels (Great, Good, OK, Poor, Terrible), required field |
| **Review Text** | Character limit (2000 characters), character counter, placeholder text "Share your experience with this item" |
| **Photo Upload** | Drag-drop zone or click to browse, crop/rotate before upload, image compression for fast loading |
| **Product Verification** | Confirm customer purchased item (verified badge), hide reviewer email, optional name (default: "Verified Buyer") |
| **Review Display** | Sort by: Most Recent, Highest Rating, Lowest Rating |
| **Review Layout** | Star rating, review text, reviewer name (or "Verified Buyer"), purchase date, image thumbnail, report button |
| **Review Moderation** | Profanity filter, detect fake/spam reviews, approval process before displaying (configurable) |
| **Review Analytics** | Average rating, rating distribution (chart showing % at each star level), total review count, total photos |
| **Filters** | Filter by star rating (1-5), verified purchase only, date range |
| **Report Review** | Button to report inappropriate content (spam, offensive, misleading), reason dropdown |
| **Buyer's Guide** | Show reviews sorted by relevance to customer's interests (if AI recommendations enabled) |
| **Review Incentives** | Optional: Prompt for review post-purchase, small incentive mention (loyalty points if enabled), privacy notice on incentivized reviews |

### View 23: Customer Account & Profile

| Attribute | Value |
|-----------|-------|
| **View Name** | Customer Account & Profile Management |
| **View Code** | CUST_008 |
| **Purpose** | Manage customer profile, saved addresses, payment methods, preferences, and account security |
| **Target Users** | Logged-in Customers |
| **Sidebar Navigation** | Profile Overview, My Orders, Saved Addresses, Payment Methods, Wishlist, Notifications, Account Security, Help |
| **Profile Overview Tab** | Name, email, phone, birthday (optional), profile picture (editable), customer since date, loyalty status (if applicable) |
| **Edit Profile** | Edit name, phone, birthday, profile picture, account type preferences; save changes |
| **My Orders List** | Historical orders (paginated), show: order date, order ID, total, status, reorder button, leave review button |
| **Saved Addresses** | List of saved addresses (billing/shipping), set default address, add new address, edit address, delete address |
| **Address Form** | Full name, phone, street address, city, state, ZIP, country, address label (Home/Work/Other), set as default checkbox |
| **Payment Methods (Future v2)** | Saved cards are out of scope for v1; payment details are entered at checkout only. |
| **Wishlist Tab (Future v2)** | Saved products grid/list, remove from wishlist, move to cart, check if now in stock (notify button), price change notification |
| **Notifications Settings** | Email notification preferences (order updates, promotions, reviews, product back-in-stock) |
| **Notification Types** | Order status changes, new promotions, abandoned cart reminders, personalized recommendations, product review requests |
| **Account Security** | Change password, two-factor authentication (2FA) setup/management, view active sessions, logout all devices option |
| **Password Change** | Enter current password, new password, confirm new password, password strength indicator, save change |
| **2FA Setup (Future v2)** | Optional 2FA with SMS or authenticator app; not required in v1. |
| **Login Activity** | Recent login history (date, time, device, IP address, location), suspicious activity alerts |
| **Privacy Settings** | Control data sharing, personalization preferences, allow third-party communications, cookie preferences |
| **Help & Support** | FAQ access, contact support form, chatbot assistance, help center link, refund policy link |
| **Account Deletion** | Option to delete account (permanent), warning about data loss, confirmation required |
| **Mobile View** | Responsive layout, collapsible sections, swipe-friendly navigation, optimized form entry |
| **Data Download** | Option to download account data (export as CSV), GDPR compliance support |

### View 24: AI Recommendation Chat

| Attribute | Value |
|-----------|-------|
| **View Name** | AI Recommendation & Chat Assistant |
| **View Code** | CUST_009 |
| **Purpose** | Provide personalized product recommendations via conversational AI, assist with product discovery |
| **Target Users** | All Customers (Guest & Logged-in) |
| **Access Point** | Chat bubble button (bottom-right), accessible from all pages, minimizable |
| **Chat Interface** | Message history, user and AI messages clearly distinguished, typing indicator, send button/enter key, emoji support |
| **AI Assistant Name** | SeShop Assistant (or brand-specific name), avatar image, friendly greeting on first interaction |
| **Conversation Starter** | "What are you looking for?" OR "Tell me about your style" OR "Recommend something for..." |
| **User Input Types** | Free-form text (e.g., "I need a dress for summer"), predefined options (e.g., "Browse by occasion" with buttons), follow-up questions with buttons |
| **AI Recommendation Logic** | Analyze user preferences → Filter available inventory → Rank by relevance/popularity → Show top 3-5 products with images, names, prices |
| **Product Cards in Chat** | Show product thumbnail, name, price, star rating, quick view link, add to cart button, TRY/SWAP buttons for alternatives |
| **Conversational Flow** | Multi-turn conversation (context memory), clarifying questions if needed, handle follow-ups naturally |
| **Recommendation Criteria** | User style/preference (casual, formal, sporty), size/fit preference, color preference, price range, occasion, material preference |
| **Size Recommendation** | If user mentions size or provides measurements, suggest appropriate sizes based on product fit guide |
| **Personalization** | Remember user preferences in session, offer tailored recommendations if logged in (use past purchases/wishlist) |
| **Product Links** | Click product → Opens product detail page in modal or new tab, seamless experience |
| **Quick Actions** | Add to cart button in chat, add to wishlist heart icon, get size recommendation link, invite friend to shop |
| **Handoff to Support** | If user asks something outside AI scope or requests live support, offer escalation to human agent |
| **Help & Examples** | "Examples of things you can ask:", error handling with suggestions if query is unclear |
| **Privacy** | Privacy notice in chat (data collection/usage), option to opt-out, clear chat history button |
| **Performance** | Fast response time (<2 sec., with loading indicator), graceful error handling with retry option |
| **Offline Fallback** | If chat service unavailable, show alternative (static recommendations, help access) |
| **Feedback** | Thumbs up/down on AI response, option to report incorrect recommendation, feedback collected for improvement |

### View 25: Wishlist

| Attribute | Value |
|-----------|-------|
| **View Name** | Customer Wishlist Management |
| **View Code** | CUST_010 |
| **Purpose** | Save favorite products for future purchase, track price changes, and enable gift sharing |
| **Scope** | Future (v2); not required for v1 implementation |
| **Target Users** | Logged-in Customers |
| **Access Point** | Top navigation menu, "Wishlist" link, heart icon in header showing count |
| **Wishlist Display** | Grid or list view, toggle option for view preference; paginated if large list |
| **Product Card in Wishlist** | Product image, name, price (highlight if on sale), star rating, remove button, add to cart button, move to cart CTA |
| **Price Change Indicator** | If product price decreased, show original price (strikethrough) and new sale price with "Sale" badge, notify customer if enabled |
| **Back in Stock** | If product was out of stock, show "Now In Stock" badge, option to request notification if still not available |
| **Sharing Wishlist** | Share entire wishlist (via unique link, email, SMS, social media) for gift giving, recipient can view but not edit |
| **Shared Wishlist** | Mark items as "purchased" so gift givers know what's already bought (optional feature) |
| **Wishlist Count** | Show quantity of items in wishlist, clear count on Wishlist page load |
| **Remove Item** | "X" button to remove item from wishlist, confirmation dialog for accidental removal |
| **Bulk Actions** | Select multiple items → "Add All to Cart" button, "Delete All" option |
| **Sort & Filter** | Sort by: date added, price (low to high), price (high to low), rating, newest |
| **Wishlist ID** | Generate unique wishlist URL if shared, easy copy-to-clipboard, QR code generation for sharing |
| **Notifications** | Notify when item on sale (if enabled), notify when item back in stock (if enabled), email digest of wishlist changes |
| **Mobile Optimize** | Different view showing card layout, swipe to remove item, prominent add to cart button |
| **Empty Wishlist** | Friendly message, link to shop/browse products, suggested categories to explore |
| **Integration** | Heart Icon on product pages shows if in wishlist, 1-click add/remove from product pages and PLP |

---

## Legend & Key Terms

| Term | Definition |
|------|-----------|
| **SKU** | Stock Keeping Unit - unique identifier for a product variant (size + color combination) |
| **POS** | Point of Sale - in-store transaction terminal for retail cashier operations |
| **User Stories** | Detailed descriptions of user needs and acceptance criteria from BRD/SRS |
| **Omnichannel** | Unified shopping experience across online (e-commerce) and offline (physical stores) channels |
| **RBAC** | Role-Based Access Control - permission system based on assigned roles for staff |
| **CoD** | Cash on Delivery - payment method where customer pays when order arrives |
| **OAuth** | Open Authentication - secure third-party connection (e.g., Instagram account linking) |
| **Stripe** | Third-party payment processor for online credit/debit card transactions |
| **Fulfillment** | Order processing and shipment preparation from inventory locations |
| **Allocation** | Assignment of online order to a specific physical location for fulfillment |
| **Transfer Order** | Inter-location stock movement request (store-to-store, store-to-storage, etc.) |
| **Draft** | Unsaved or unapproved content (e.g., Instagram post draft awaiting review) |
| **Sync** | Data synchronization between systems to keep information up-to-date |

---

## Notes for Development Teams

- **Responsive Design**: All views must be mobile-friendly with touch optimization for tablets and phones.
- **Accessibility**: WCAG 2.1 AA compliance required for all customer-facing views.
- **Performance**: Load times < 3 seconds on 4G, images optimized via CDN or lazy loading.
- **Localization**: Prepare for multi-language support (text labels, error messages, regional formats).
- **Error Handling**: Graceful error messages with actionable steps for users.
- **Notifications**: Real-time updates where applicable; email/SMS templates required for transactional notifications.
- **Testing**: Each view should be tested with both happy-path and edge-case scenarios per QA plan.

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-27  
**Next Review Date:** 2026-06-27
