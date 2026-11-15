-- Dormitory Management System (MySQL 8.x)
-- NOTE: Execute in MySQL 8.x. Default charset: utf8mb4.

DROP DATABASE IF EXISTS dormitory_system;
CREATE DATABASE dormitory_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE dormitory_system;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_key VARCHAR(50) NOT NULL,
  role_name VARCHAR(100) NOT NULL,
  description VARCHAR(255) NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_role_role_key (role_key)
) ENGINE=InnoDB;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(100) NOT NULL,
  phone VARCHAR(30) NULL,
  email VARCHAR(100) NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB;

CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB;

CREATE TABLE sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  perm_key VARCHAR(100) NOT NULL,
  perm_name VARCHAR(150) NOT NULL,
  perm_type VARCHAR(20) NOT NULL DEFAULT 'API',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_permission_perm_key (perm_key)
) ENGINE=InnoDB;

CREATE TABLE sys_role_permission (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
  CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
) ENGINE=InnoDB;

CREATE TABLE sys_dict (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dict_key VARCHAR(50) NOT NULL,
  dict_name VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_dict_dict_key (dict_key)
) ENGINE=InnoDB;

CREATE TABLE sys_dict_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dict_id BIGINT NOT NULL,
  item_key VARCHAR(50) NOT NULL,
  item_value VARCHAR(100) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_dict_item (dict_id, item_key),
  CONSTRAINT fk_dict_item_dict FOREIGN KEY (dict_id) REFERENCES sys_dict(id)
) ENGINE=InnoDB;

CREATE TABLE system_setting (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  setting_key VARCHAR(100) NOT NULL,
  setting_value VARCHAR(500) NULL,
  description VARCHAR(255) NULL,
  updated_by BIGINT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_system_setting_key (setting_key),
  CONSTRAINT fk_system_setting_user FOREIGN KEY (updated_by) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE dorm_building (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  building_code VARCHAR(30) NOT NULL,
  building_name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NULL,
  manager_user_id BIGINT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_dorm_building_code (building_code),
  CONSTRAINT fk_building_manager_user FOREIGN KEY (manager_user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE dorm_room (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  building_id BIGINT NOT NULL,
  floor_no INT NOT NULL,
  room_no VARCHAR(20) NOT NULL,
  room_type VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
  capacity INT NOT NULL DEFAULT 4,
  gender_limit VARCHAR(10) NOT NULL DEFAULT 'MIX',
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_dorm_room (building_id, room_no),
  KEY idx_dorm_room_building (building_id),
  CONSTRAINT fk_room_building FOREIGN KEY (building_id) REFERENCES dorm_building(id)
) ENGINE=InnoDB;

CREATE TABLE bed (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_id BIGINT NOT NULL,
  bed_no VARCHAR(10) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'EMPTY',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_bed (room_id, bed_no),
  KEY idx_bed_room (room_id),
  CONSTRAINT fk_bed_room FOREIGN KEY (room_id) REFERENCES dorm_room(id)
) ENGINE=InnoDB;

CREATE TABLE student (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  student_no VARCHAR(30) NOT NULL,
  name VARCHAR(100) NOT NULL,
  gender VARCHAR(10) NOT NULL,
  college VARCHAR(100) NULL,
  major VARCHAR(100) NULL,
  class_name VARCHAR(100) NULL,
  phone VARCHAR(30) NULL,
  id_card_no VARCHAR(30) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_student_student_no (student_no),
  UNIQUE KEY uk_student_user (user_id),
  CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE bed_assignment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  bed_id BIGINT NOT NULL,
  check_in_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  check_out_at DATETIME NULL,
  reason VARCHAR(255) NULL,
  created_by BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_assignment_student (student_id),
  KEY idx_assignment_bed (bed_id),
  CONSTRAINT fk_assignment_student FOREIGN KEY (student_id) REFERENCES student(id),
  CONSTRAINT fk_assignment_bed FOREIGN KEY (bed_id) REFERENCES bed(id),
  CONSTRAINT fk_assignment_created_by FOREIGN KEY (created_by) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE repair_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(40) NOT NULL,
  student_id BIGINT NOT NULL,
  room_id BIGINT NOT NULL,
  title VARCHAR(150) NOT NULL,
  description TEXT NULL,
  photos_json JSON NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
  priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
  assigned_worker_user_id BIGINT NULL,
  assigned_at DATETIME NULL,
  finished_at DATETIME NULL,
  rating INT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_repair_order_no (order_no),
  KEY idx_repair_student (student_id),
  KEY idx_repair_room (room_id),
  KEY idx_repair_status (status),
  CONSTRAINT fk_repair_student FOREIGN KEY (student_id) REFERENCES student(id),
  CONSTRAINT fk_repair_room FOREIGN KEY (room_id) REFERENCES dorm_room(id),
  CONSTRAINT fk_repair_worker FOREIGN KEY (assigned_worker_user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE repair_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  repair_order_id BIGINT NOT NULL,
  action VARCHAR(30) NOT NULL,
  from_status VARCHAR(20) NULL,
  to_status VARCHAR(20) NULL,
  note VARCHAR(500) NULL,
  operator_user_id BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_repair_log_order (repair_order_id),
  CONSTRAINT fk_repair_log_order FOREIGN KEY (repair_order_id) REFERENCES repair_order(id),
  CONSTRAINT fk_repair_log_operator FOREIGN KEY (operator_user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE visitor_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  building_id BIGINT NOT NULL,
  room_id BIGINT NULL,
  visited_student_id BIGINT NULL,
  visitor_name VARCHAR(100) NOT NULL,
  visitor_phone VARCHAR(30) NULL,
  visitor_id_card VARCHAR(30) NULL,
  relation_desc VARCHAR(100) NULL,
  visit_reason VARCHAR(255) NULL,
  in_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  out_time DATETIME NULL,
  registrar_user_id BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_visitor_building (building_id),
  KEY idx_visitor_in_time (in_time),
  CONSTRAINT fk_visitor_building FOREIGN KEY (building_id) REFERENCES dorm_building(id),
  CONSTRAINT fk_visitor_room FOREIGN KEY (room_id) REFERENCES dorm_room(id),
  CONSTRAINT fk_visitor_student FOREIGN KEY (visited_student_id) REFERENCES student(id),
  CONSTRAINT fk_visitor_registrar FOREIGN KEY (registrar_user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE late_return_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  late_time DATETIME NOT NULL,
  reason VARCHAR(255) NULL,
  handled_by BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_late_student (student_id),
  KEY idx_late_time (late_time),
  CONSTRAINT fk_late_student FOREIGN KEY (student_id) REFERENCES student(id),
  CONSTRAINT fk_late_handler FOREIGN KEY (handled_by) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(150) NOT NULL,
  content TEXT NOT NULL,
  target_type VARCHAR(20) NOT NULL DEFAULT 'ALL',
  target_value VARCHAR(100) NULL,
  publisher_user_id BIGINT NULL,
  publish_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_notice_publish_at (publish_at),
  CONSTRAINT fk_notice_publisher FOREIGN KEY (publisher_user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE notification_read (
  notification_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (notification_id, user_id),
  CONSTRAINT fk_notice_read_notice FOREIGN KEY (notification_id) REFERENCES notification(id),
  CONSTRAINT fk_notice_read_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

CREATE TABLE operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  action VARCHAR(100) NOT NULL,
  module VARCHAR(50) NULL,
  request_path VARCHAR(200) NULL,
  request_method VARCHAR(10) NULL,
  ip VARCHAR(50) NULL,
  success TINYINT NOT NULL DEFAULT 1,
  message VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_oplog_created_at (created_at),
  KEY idx_oplog_user (user_id),
  CONSTRAINT fk_oplog_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB;

-- seed roles
INSERT INTO sys_role(role_key, role_name, description) VALUES
('ADMIN','管理员','系统管理员'),
('STUDENT','学生','学生用户'),
('WORKER','维修工','维修工用户'),
('DORM_MANAGER','宿管员','宿舍管理员');

-- seed dicts
INSERT INTO sys_dict(dict_key, dict_name) VALUES
('bed_status','床位状态'),
('repair_status','报修状态'),
('gender','性别');

INSERT INTO sys_dict_item(dict_id, item_key, item_value, sort_no) VALUES
((SELECT id FROM sys_dict WHERE dict_key='bed_status'),'EMPTY','空床',1),
((SELECT id FROM sys_dict WHERE dict_key='bed_status'),'OCCUPIED','已入住',2),
((SELECT id FROM sys_dict WHERE dict_key='bed_status'),'DISABLED','停用',3),
((SELECT id FROM sys_dict WHERE dict_key='repair_status'),'SUBMITTED','已提交',1),
((SELECT id FROM sys_dict WHERE dict_key='repair_status'),'ASSIGNED','已派单',2),
((SELECT id FROM sys_dict WHERE dict_key='repair_status'),'IN_PROGRESS','处理中',3),
((SELECT id FROM sys_dict WHERE dict_key='repair_status'),'FINISHED','已完成',4),
((SELECT id FROM sys_dict WHERE dict_key='repair_status'),'CLOSED','已关闭',5),
((SELECT id FROM sys_dict WHERE dict_key='gender'),'M','男',1),
((SELECT id FROM sys_dict WHERE dict_key='gender'),'F','女',2);

SET FOREIGN_KEY_CHECKS = 1;
