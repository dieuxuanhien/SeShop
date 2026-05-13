UPDATE users
SET password_hash = '$2a$12$mu6eqOVHAemcEQLpbw8rQeF1rX.3rWWisehXzshT8C7HuV.XIdoxO',  
    updated_at = NOW()
WHERE username = 'super.admin';

UPDATE users
SET password_hash = '$2a$12$mu6eqOVHAemcEQLpbw8rQeF1rX.3rWWisehXzshT8C7HuV.XIdoxO', 
    updated_at = NOW()
WHERE username = 'staff.manager';

UPDATE users
SET password_hash = '$2a$12$mu6eqOVHAemcEQLpbw8rQeF1rX.3rWWisehXzshT8C7HuV.XIdoxO',  
    updated_at = NOW()
WHERE username = 'demo.customer';


-- ALL password are Strong@123