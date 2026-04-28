<img src="media/image1.png" style="width:6.49375in;height:5.77569in" />

Bảng User

| STT | Tên thuộc tính | Kiểu         | Ràng buộc        |
|-----|----------------|--------------|------------------|
| 1   | id             | BIG INT      | PK, NOT NULL     |
| 2   | username       | VARCHAR(255) | UNIQUE, NOT NULL |
| 3   | phone_number   | VARCHAR(255) | NOT NULL         |
| 4   | email          | VARCHAR(255) | UNIQUE, NOT NULL |
| 5   | rating         | INT          |                  |
| 6   | fullname       | VARCHAR(255) |                  |
| 7   | address        | VARCHAR(255) |                  |
| 8   | created_date   | datetime     | NOT NULL         |
| 9   | updated_date   | datetime     | NOT NULL         |

Bảng Admin

| STT | Tên thuộc tính | Kiểu         | Ràng buộc        |
|-----|----------------|--------------|------------------|
| 1   | id             | BIG INT      | PK, NOT NULL     |
| 2   | username       | VARCHAR(255) | UNIQUE, NOT NULL |
| 3   | phone_number   | VARCHAR(255) | NOT NULL         |
| 4   | email          | VARCHAR(255) | UNIQUE, NOT NULL |
| 5   | fullname       | VARCHAR(255) |                  |
| 6   | created_date   | datetime     | NOT NULL         |
| 7   | updated_date   | datetime     | NOT NULL         |

Bảng CustomerService

| STT | Tên thuộc tính | Kiểu         | Ràng buộc        |
|-----|----------------|--------------|------------------|
| 1   | id             | BIG INT      | PK, NOT NULL     |
| 2   | username       | VARCHAR(255) | UNIQUE, NOT NULL |
| 3   | phone_number   | VARCHAR(255) | NOT NULL         |
| 4   | email          | VARCHAR(255) | UNIQUE, NOT NULL |
| 5   | fullname       | VARCHAR(255) |                  |
| 6   | created_date   | datetime     | NOT NULL         |
| 7   | updated_date   | datetime     | NOT NULL         |

Bảng Category

| STT | Tên thuộc tính | Kiểu         | Ràng buộc    |
|-----|----------------|--------------|--------------|
| 1   | id             | BIG INT      | PK, NOT NULL |
| 2   | name           | VARCHAR(255) | NOT NULL     |
| 3   | description    | VARCHAR(255) |              |
| 4   | created_date   | datetime     | NOT NULL     |
| 5   | updated_date   | datetime     | NOT NULL     |

Bảng Product

| STT | Tên thuộc tính | Kiểu         | Ràng buộc    |
|-----|----------------|--------------|--------------|
| 1   | id             | BIG INT      | PK, NOT NULL |
| 2   | name           | VARCHAR(255) | NOT NULL     |
| 3   | description    | VARCHAR(255) |              |
| 4   | brand          | VARCHAR(255) |              |
| 5   | created_date   | datetime     | NOT NULL     |
| 6   | updated_date   | datetime     | NOT NULL     |

Bảng Product_Category

| STT | Tên thuộc tính | Kiểu     | Ràng buộc        |
|-----|----------------|----------|------------------|
| 1   | product_id     | BIG INT  | PK,FK, NOT NULL  |
| 2   | category_id    | BIG INT  | PK, FK, NOT NULL |
| 3   | created_date   | datetime | NOT NULL         |
| 4   | updated_date   | datetime | NOT NULL         |

Bảng Post

| STT | Tên thuộc tính | Kiểu         | Ràng buộc    |
|-----|----------------|--------------|--------------|
| 1   | id             | BIG INT      | PK, NOT NULL |
| 2   | user_id        | BIG INT      | FK, NOT NULL |
| 3   | images         | JSON         |              |
| 4   | product_id     | BIG INT      | FK, NOT NULL |
| 5   | price          | DECIMAL      | NOT NULL     |
| 6   | title          | VARCHAR(255) | NOT NULL     |
| 7   | description    | VARCHAR(255) |              |
| 8   | area           | VARCHAR(255) | NOT NULL     |
| 9   | status         | ENUM         | NOT NULL     |
| 10  | created_date   | datetime     | NOT NULL     |
| 11  | updated_date   | datetime     | NOT NULL     |

Bảng Rating

| STT | Tên thuộc tính | Kiểu         | Ràng buộc    |
|-----|----------------|--------------|--------------|
| 1   | id             | BIG INT      | PK, NOT NULL |
| 2   | buyer_id       | BIG INT      | FK, NOT NULL |
| 3   | post_id        | BIG INT      | FK, NOT NULL |
| 4   | rating         | INT          | NOT NULL     |
| 5   | comment        | VARCHAR(255) | NOT NULL     |
| 6   | created_date   | datetime     | NOT NULL     |
| 7   | updated_date   | datetime     | NOT NULL     |

Bảng Feedback

| STT | Tên thuộc tính | Kiểu         | Ràng buộc    |
|-----|----------------|--------------|--------------|
| 1   | id             | BIG INT      | PK, NOT NULL |
| 2   | user_id        | BIG INT      | FK, NOT NULL |
| 3   | category       | ENUM         | NOT NULL     |
| 4   | proof          | VARCHAR(255) |              |
| 5   | comment        | VARCHAR(255) | NOT NULL     |
| 6   | handler_id     | BIG INT      | PK           |
| 7   | reply          | VARCHAR(255) |              |
| 8   | created_date   | datetime     | NOT NULL     |
| 9   | updated_date   | datetime     | NOT NULL     |

Bảng PushPlan

| STT | Tên thuộc tính | Kiểu         | Ràng buộc    |
|-----|----------------|--------------|--------------|
| 1   | id             | BIG INT      | PK, NOT NULL |
| 2   | post_id        | BIG INT      | FK, NOT NULL |
| 3   | rule           | datetime     | NOT NULL     |
| 4   | timeframe      | VARCHAR(255) | NOT NULL     |
| 5   | created_date   | datetime     | NOT NULL     |
| 6   | updated_date   | datetime     | NOT NULL     |

Bảng PushPlanPayment

| STT | Tên thuộc tính | Kiểu         | Ràng buộc    |
|-----|----------------|--------------|--------------|
| 1   | id             | BIG INT      | PK, NOT NULL |
| 2   | push_plan_id   | BIG INT      | FK, NOT NULL |
| 3   | created_date   | datetime     | NOT NULL     |
| 4   | provider       | VARCHAR(255) | NOT NULL     |
| 5   | status         | ENUM         | NOT NULL     |
| 6   | method         | ENUM         | NOT NULL     |
| 7   | created_date   | datetime     | NOT NULL     |
| 8   | updated_date   | datetime     | NOT NULL     |

Bảng Order

| STT | Tên thuộc tính | Kiểu     | Ràng buộc    |
|-----|----------------|----------|--------------|
| 1   | id             | BIG INT  | PK, NOT NULL |
| 2   | created_date   | datetime | NOT NULL     |
| 3   | post_id        | BIG INT  | FK, NOT NULL |
| 4   | status         | ENUM     | NOT NULL     |
| 5   | total          | NUMERIC  | NOT NULL     |
| 6   | buyer_id       | BIG INT  | FK, NOT NULL |
| 7   | updated_date   | datetime | NOT NULL     |

Bảng Shipment

| STT | Tên thuộc tính | Kiểu         | Ràng buộc    |
|-----|----------------|--------------|--------------|
| 1   | id             | BIG INT      | PK, NOT NULL |
| 2   | order_id       | BIG INT      | FK, NOT NULL |
| 3   | status         | ENUM         | NOT NULL     |
| 4   | cost           | NUMERIC      | NOT NULL     |
| 5   | supplier       | VARCHAR(255) | NOT NULL     |
| 6   | destination    | VARCHAR(255) | NOT NULL     |
| 7   | pickup_date    | datetime     |              |
| 8   | delivery_date  | datetime     |              |
| 9   | created_date   | datetime     | NOT NULL     |
| 10  | updated_date   | datetime     | NOT NULL     |

Bảng Payment

| STT | Tên thuộc tính | Kiểu         | Ràng buộc    |
|-----|----------------|--------------|--------------|
| 1   | id             | BIG INT      | PK, NOT NULL |
| 2   | order_id       | BIG INT      | FK, NOT NULL |
| 3   | created_date   | datetime     | NOT NULL     |
| 4   | provider       | VARCHAR(255) | NOT NULL     |
| 5   | status         | ENUM         | NOT NULL     |
| 6   | method         | ENUM         | NOT NULL     |
| 7   | updated_date   | datetime     | NOT NULL     |
