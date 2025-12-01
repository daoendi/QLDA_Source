DROP DATABASE IF EXISTS library_management;

CREATE DATABASE IF NOT EXISTS library_management;

USE library_management;

CREATE TABLE members (
    member_id VARCHAR(10) NOT NULL,
    firstName VARCHAR(45),
    lastName VARCHAR(45),
    gender VARCHAR(10),
    birth DATE,
    email VARCHAR(100),
    phone VARCHAR(15),
    image VARCHAR(100),
    PRIMARY KEY (member_id)
);

CREATE TABLE member_id_counter (
    id INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id)
);

CREATE TABLE admins (
    admin_id INT NOT NULL AUTO_INCREMENT,
    admin_name VARCHAR(45) NOT NULL UNIQUE,
    firstName VARCHAR(45),
    lastName VARCHAR(45),
    password VARCHAR(45),
    gender VARCHAR(10),
    birth DATE,
    email VARCHAR(100),
    phone VARCHAR(15),
    image VARCHAR(100),
    PRIMARY KEY (admin_id)
);

CREATE TABLE books (
    bookmark VARCHAR(45),
    book_id VARCHAR(10) NOT NULL,
    book_name VARCHAR(45),
    book_author VARCHAR(45),
    price DOUBLE,
    time_of_borrow INT,
    quantity_in_store INT,
    borrowing INT DEFAULT 0,
    remaining_quantity INT,
    PRIMARY KEY (book_id)
);

CREATE TABLE book_id_counter (
    id INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id)
);

CREATE TABLE borrow (
    id INT NOT NULL AUTO_INCREMENT,
    member_id VARCHAR(10) NOT NULL,
    book_id VARCHAR(10) NOT NULL,
    book_name VARCHAR(45),
    borrow_date DATE,
    returned_date DATE,
    status VARCHAR(10),
    PRIMARY KEY (id),
    CONSTRAINT fk_member_id_1 FOREIGN KEY (member_id) REFERENCES members (member_id)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    CONSTRAINT fk_book_id_1 FOREIGN KEY (book_id) REFERENCES books(book_id)
        ON UPDATE CASCADE
        ON DELETE NO ACTION
);

CREATE TABLE payment (
    id INT NOT NULL AUTO_INCREMENT,
    member_id VARCHAR(10) NOT NULL,
    book_id VARCHAR(10) NOT NULL,
    book_name VARCHAR(45),
    order_date DATETIME,
    quantity_of_order INT NOT NULL,
    price DOUBLE,
    payment DOUBLE,
    PRIMARY KEY (id),
    CONSTRAINT fk_userID_2 FOREIGN KEY (member_id) REFERENCES members (member_id)
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
    CONSTRAINT fk_book_id_2 FOREIGN KEY (book_id) REFERENCES books(book_id)
        ON UPDATE CASCADE
        ON DELETE NO ACTION
);

-- tự động định thêm image cho admin
DELIMITER $$

CREATE TRIGGER before_insert_admin
BEFORE INSERT ON admins
FOR EACH ROW
BEGIN
    SET NEW.image = CONCAT('/img/avt/admin/image', FLOOR(1 + RAND() * 4), '.jpg');
END$$

DELIMITER ;

-- tự động định dạng lại member_id
DELIMITER $$

CREATE TRIGGER before_insert_members
BEFORE INSERT ON members
FOR EACH ROW
BEGIN
    INSERT INTO member_id_counter () VALUES ();
    SET NEW.member_id = LPAD(LAST_INSERT_ID(), 6, '0');
    SET NEW.image = CONCAT('/img/avt/member/image', FLOOR(1 + RAND() * 10), '.jpg');
END$$

DELIMITER ;

-- tự động cập nhập số sách còn lại
DELIMITER $$

CREATE TRIGGER update_remaining_quantity_before_insert
BEFORE INSERT ON books
FOR EACH ROW
BEGIN
    SET NEW.remaining_quantity = NEW.quantity_in_store - NEW.borrowing;
END$$

CREATE TRIGGER update_remaining_quantity_before_update
BEFORE UPDATE ON books
FOR EACH ROW
BEGIN
    SET NEW.remaining_quantity = NEW.quantity_in_store - NEW.borrowing;
END$$

CREATE TRIGGER before_insert_book
BEFORE INSERT ON books
FOR EACH ROW
BEGIN
    INSERT INTO book_id_counter () VALUES ();
    SET NEW.book_id = CONCAT('B0', LPAD(LAST_INSERT_ID(), 2, '0'));
END$$

DELIMITER ;

-- tự động cập nhập số lần mượn từng loại sách
DELIMITER $$

CREATE TRIGGER update_time_of_borrow
AFTER INSERT ON borrow
FOR EACH ROW
BEGIN
    UPDATE books
    SET time_of_borrow = (
        SELECT COUNT(*)
        FROM borrow
        WHERE book_id = NEW.book_id
    )
    WHERE book_id = NEW.book_id;
END$$

DELIMITER ;

-- tự động cập nhập số sách đang được mượn
DELIMITER $$

CREATE TRIGGER update_borrowing_count_on_insert
AFTER INSERT ON borrow
FOR EACH ROW
BEGIN
    UPDATE books
    SET borrowing = (
        SELECT COUNT(*)
        FROM borrow
        WHERE book_id = NEW.book_id AND returned_date IS NULL
    )
    WHERE book_id = NEW.book_id;
END$$

CREATE TRIGGER update_borrowing_count_on_update
AFTER UPDATE ON borrow
FOR EACH ROW
BEGIN
    UPDATE books
    SET borrowing = (
        SELECT COUNT(*)
        FROM borrow
        WHERE book_id = NEW.book_id AND returned_date IS NULL
    )
    WHERE book_id = NEW.book_id;
END$$

CREATE TRIGGER update_borrowing_count_on_delete
AFTER DELETE ON borrow
FOR EACH ROW
BEGIN
    UPDATE books
    SET borrowing = (
        SELECT COUNT(*)
        FROM borrow
        WHERE book_id = OLD.book_id AND returned_date IS NULL
    )
    WHERE book_id = OLD.book_id;
END$$

DELIMITER ;

-- tự động điền tên sách
DELIMITER $$

CREATE TRIGGER set_borrow_defaults_in_borrow
BEFORE INSERT ON borrow
FOR EACH ROW
BEGIN
    DECLARE temp_book_name VARCHAR(45);

    SELECT book_name
    INTO temp_book_name
    FROM books
    WHERE book_id = NEW.book_id;

    SET NEW.book_name = temp_book_name;
END$$


DELIMITER ;

-- tự động cập nhập trạng thái của sách
DELIMITER $$

CREATE TRIGGER update_borrow_status_insert
BEFORE INSERT ON borrow
FOR EACH ROW
BEGIN
    IF NEW.returned_date IS NOT NULL THEN
        SET NEW.status = 'returned';
    ELSEIF NEW.borrow_date IS NOT NULL AND DATEDIFF(CURDATE(), NEW.borrow_date) > 14 THEN
        SET NEW.status = 'overdue';
    ELSE
        SET NEW.status = 'borrowing';
    END IF;
END$$

DELIMITER ;

-- tự động cập nhập trạng thái của sách
DELIMITER $$

CREATE TRIGGER update_borrow_status_update
BEFORE UPDATE ON borrow
FOR EACH ROW
BEGIN
    IF NEW.returned_date IS NOT NULL THEN
        SET NEW.status = 'returned';
    ELSEIF NEW.borrow_date IS NOT NULL AND DATEDIFF(CURDATE(), NEW.borrow_date) > 14 THEN
        SET NEW.status = 'overdue';
    ELSE
        SET NEW.status = 'borrowing';
    END IF;
END$$

DELIMITER ;

-- tự động cập nhập thông tin đơn hàng
DELIMITER $$

CREATE TRIGGER trg_payment_before_insert
BEFORE INSERT ON payment
FOR EACH ROW
BEGIN
    DECLARE temp_book_name VARCHAR(45);
    DECLARE temp_price DOUBLE;

    -- Lấy tên sách và giá sách từ bảng books
    SELECT book_name, price INTO temp_book_name, temp_price
    FROM books
    WHERE book_id = NEW.book_id;
    SET new.book_name = temp_book_name, new.price = temp_price;

    -- Tính toán tổng tiền thanh toán
    SET NEW.payment = NEW.quantity_of_order * NEW.price;

    UPDATE books
    SET quantity_in_store = quantity_in_store - NEW.quantity_of_order
    WHERE book_id = NEW.book_id;
END$$

DELIMITER ;

-- chèn dữ liệu mặc định cho borrow
DELIMITER $$

CREATE PROCEDURE insert_random_borrows()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE member_id VARCHAR(10);
    DECLARE book_id VARCHAR(10);
    DECLARE borrow_date DATE;

    WHILE i < 100 DO
        -- Sinh mã member_id ngẫu nhiên
        SET member_id = LPAD(FLOOR(1 + RAND() * 20), 6, '0');

        -- Sinh mã book_id ngẫu nhiên
        SET book_id = CONCAT('B0', LPAD(FLOOR(1 + RAND() * 20), 2, '0'));

        SET borrow_date = DATE_FORMAT(CONCAT('2024-11-', LPAD(FLOOR(1 + RAND() * 30), 2, '0')), '%Y-%m-%d');

        -- Chèn bản ghi vào bảng borrow
        INSERT INTO borrow (member_id, book_id, borrow_date)
        VALUES (member_id, book_id, borrow_date);

        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

-- chèn dữ liệu mặc định cho payment
DELIMITER $$

CREATE PROCEDURE insert_random_payment()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE member_id VARCHAR(10);
    DECLARE book_id VARCHAR(10);
    DECLARE quantity INT;
    DECLARE order_date DATETIME;

    WHILE i < 100 DO
        -- Sinh mã member_id ngẫu nhiên
        SET member_id = LPAD(FLOOR(1 + RAND() * 20), 6, '0');

        -- Sinh mã book_id ngẫu nhiên
        SET book_id = CONCAT('B0', LPAD(FLOOR(1 + RAND() * 20), 2, '0'));

        -- Sinh số lượng ngẫu nhiên
        SET quantity = 1 + RAND() * 3;

        -- Sinh ngày giờ mua ngẫu nhiên
        SET order_date = CONCAT(
            '2024-11-',
            LPAD(FLOOR(1 + RAND() * 30), 2, '0'), ' ',
            LPAD(FLOOR(RAND() * 24), 2, '0'), ':',
            LPAD(FLOOR(RAND() * 60), 2, '0'), ':',
            LPAD(FLOOR(RAND() * 60), 2, '0')
        );

        -- Chèn bản ghi vào bảng payment
        INSERT INTO payment (member_id, book_id, quantity_of_order, order_date)
        VALUES (member_id, book_id, quantity, order_date);

        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

-- Dữ liệu mẫu
INSERT INTO members (firstName, lastName, gender, birth, email, phone)
VALUES
('John', 'Doe', 'Male', '1990-01-15', 'john.doe@gmail.com', '0123456789'),
('Jane', 'Smith', 'Female', '1995-07-20', 'jane.smith@gmail.com', '0987654321'),
('Alice', 'Brown', 'Female', '1988-11-05', 'alice.brown@gmail.com', '0123456788'),
('Bob', 'Jones', 'Male', '1992-03-30', 'bob.jones@gmail.com', '0123456799'),
('Charlie', 'Clark', 'Male', '1998-12-10', 'charlie.clark@gmail.com', '0987654322'),
('Emily', 'Davis', 'Female', '1993-04-25', 'emily.davis@gmail.com', '0123456787'),
('Frank', 'Moore', 'Male', '1985-09-18', 'frank.moore@gmail.com', '0987654323'),
('Grace', 'Hill', 'Female', '1991-06-12', 'grace.hill@gmail.com', '0123456798'),
('Henry', 'Wilson', 'Male', '1987-10-28', 'henry.wilson@gmail.com', '0987654324'),
('Isabel', 'Martin', 'Female', '1999-02-14', 'isabel.martin@gmail.com', '0123456786'),
('Liam', 'Anderson', 'Male', '1990-03-17', 'liam.anderson@gmail.com', '0123456785'),
('Olivia', 'Walker', 'Female', '1991-05-12', 'olivia.walker@gmail.com', '0987654325'),
('Noah', 'Harris', 'Male', '1989-09-14', 'noah.harris@gmail.com', '0123456784'),
('Ava', 'Thompson', 'Female', '1992-11-19', 'ava.thompson@gmail.com', '0987654326'),
('Ethan', 'Martinez', 'Male', '1987-07-22', 'ethan.martinez@gmail.com', '0123456783'),
('Sophia', 'Garcia', 'Female', '1994-06-23', 'sophia.garcia@gmail.com', '0987654327'),
('Logan', 'Rodriguez', 'Male', '1996-12-28', 'logan.rodriguez@gmail.com', '0123456782'),
('Mia', 'Martinez', 'Female', '1993-10-05', 'mia.martinez@gmail.com', '0987654328'),
('Mason', 'Lee', 'Male', '1988-08-01', 'mason.lee@gmail.com', '0123456781'),
('Amelia', 'Walker', 'Female', '1985-04-07', 'amelia.walker@gmail.com', '0987654329');

INSERT INTO books (book_name, book_author, price, quantity_in_store)
VALUES
('To Kill a Mockingbird', 'Harper Lee', 150.0, 100),
('1984', 'George Orwell', 120.0, 100),
('Pride and Prejudice', 'Jane Austen', 100.0,  100),
('The Great Gatsby', 'F. Scott Fitzgerald', 130.0, 100),
('Moby Dick', 'Herman Melville', 170.0, 100),
('War and Peace', 'Leo Tolstoy', 200.0, 100),
('The Odyssey', 'Homer', 180.0,  100),
('The Catcher in the Rye', 'J.D. Salinger', 140.0,  100),
('Animal Farm', 'George Orwell', 110.0,  100),
('The Lord of the Rings', 'J.R.R. Tolkien', 300.0, 100),
('Harry Potter and the Philosopher\'s Stone', 'J.K. Rowling', 250.0,  100),
('The Alchemist', 'Paulo Coelho', 190.0,  100),
('Don Quixote', 'Miguel de Cervantes', 220.0,  100),
('Jane Eyre', 'Charlotte Brontë', 150.0, 100),
('The Book Thief', 'Markus Zusak', 170.0,  100),
('The Hobbit', 'J.R.R. Tolkien', 190.0,  100),
('Fahrenheit 451', 'Ray Bradbury', 120.0,  100),
('Brave New World', 'Aldous Huxley', 150.0,  100),
('The Kite Runner', 'Khaled Hosseini', 180.0,  100),
('The Hunger Games', 'Suzanne Collins', 200.0,  100);


call insert_random_borrows();

call insert_random_payment();
