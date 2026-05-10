UPDATE users
SET password_hash = '$2a$10$zpKFEwoNnl0x.R59DtjJnOJGagzL36OxOeJfoWQo7YT40VBBErrqi',
    updated_at = NOW()
WHERE username IN ('super.admin', 'staff.manager', 'demo.customer');
