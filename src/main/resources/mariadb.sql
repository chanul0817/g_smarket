CREATE DATABASE gsmarket;
USE gsmarket;
CREATE TABLE member_table (
                              member_id VARCHAR(30) NOT NULL PRIMARY KEY,
                              member_password VARCHAR(30) NOT NULL,
                              member_name VARCHAR(30) NOT NULL,
                              member_email VARCHAR(30) NOT NULL,
                              member_phone_number VARCHAR(30) NOT NULL,
                              member_reg_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              role ENUM('ROLE_MEMBER', 'ROLE_ADMIN') NOT NULL,
                              email_verified BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE board (
                       mno BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       content TEXT NOT NULL,
                       category VARCHAR(100) NOT NULL,
                       price INT NOT NULL,
                       location VARCHAR(100),
                       regdate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       moddate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       member_id VARCHAR(30),
                       FOREIGN KEY (member_id) REFERENCES member_table(member_id) ON DELETE CASCADE
);
CREATE TABLE board_image (
                             inum BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                             uuid VARCHAR(255) NOT NULL,
                             img_name VARCHAR(255) NOT NULL,
                             path VARCHAR(255) NOT NULL,
                             board_id BIGINT,
                             FOREIGN KEY (board_id) REFERENCES board(mno) ON DELETE CASCADE
);
CREATE TABLE like_board (
                            member_id VARCHAR(30) NOT NULL,
                            board_mno BIGINT NOT NULL,
                            PRIMARY KEY (member_id, board_mno),
                            FOREIGN KEY (member_id) REFERENCES member_table(member_id) ON DELETE CASCADE,
                            FOREIGN KEY (board_mno) REFERENCES board(mno) ON DELETE CASCADE
);
CREATE TABLE note (
                      note_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                      content TEXT NOT NULL,
                      sender_id VARCHAR(30) NOT NULL,
                      receiver_id VARCHAR(30) NOT NULL,
                      send_date DATETIME NOT NULL,
                      checked BOOLEAN NOT NULL DEFAULT FALSE,
                      FOREIGN KEY (sender_id) REFERENCES member_table(member_id) ON DELETE CASCADE,
                      FOREIGN KEY (receiver_id) REFERENCES member_table(member_id) ON DELETE CASCADE
);
CREATE TABLE auction_entity (
                                id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                seller_id VARCHAR(30) NOT NULL,
                                current_bidder_id VARCHAR(30),
                                item_name VARCHAR(255) NOT NULL,
                                start_price DOUBLE NOT NULL,
                                current_bid DOUBLE NOT NULL,
                                auction_end_time DATETIME NOT NULL,
                                is_ended BOOLEAN NOT NULL DEFAULT FALSE,
                                image_url VARCHAR(255),
                                FOREIGN KEY (seller_id) REFERENCES member_table(member_id) ON DELETE CASCADE,
                                FOREIGN KEY (current_bidder_id) REFERENCES member_table(member_id) ON DELETE SET NULL
);
ALTER TABLE member_table MODIFY COLUMN role VARCHAR(50);
