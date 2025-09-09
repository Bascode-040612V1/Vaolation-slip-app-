-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 31, 2025 at 04:39 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `student_violation_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `penalty_matrix`
--

CREATE TABLE `penalty_matrix` (
  `id` int(11) NOT NULL,
  `violation_type` varchar(255) NOT NULL,
  `offense_count` int(11) NOT NULL,
  `penalty_description` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `students`
--

CREATE TABLE `students` (
  `id` int(11) NOT NULL,
  `student_id` varchar(50) NOT NULL,
  `student_name` varchar(255) NOT NULL,
  `year_level` varchar(50) NOT NULL,
  `course` varchar(100) NOT NULL,
  `section` varchar(50) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `students`
--

INSERT INTO `students` (`id`, `student_id`, `student_name`, `year_level`, `course`, `section`, `password`, `added_at`, `updated_at`) VALUES
(1, '220342', 'Joshua P. Basco', '4th Year', 'BSCS', 'BS7MA', '$2y$10$DBvE6ujqSbiqOgYIFk1RaOMLSUQRlvAtyIQx2hNhxZkZgAwLIYyN.', '2025-07-22 06:57:35', '2025-07-23 12:43:43'),
(2, '123456', 'Joshua Pavia Basco', '4th Year', 'BSCS', 'BS8MA', '$2y$10$p20Z9rcqlk1a16suel4plu5sV8JdFzLVPg17WZEzs8n7Rj1sHJWAS', '2025-08-03 13:27:42', '2025-08-03 13:30:56'),
(3, '2021-0001', 'John Doe', '4th Year', 'BSIT', 'A', NULL, '2025-08-31 10:31:47', '2025-08-31 10:31:47'),
(4, '2021-0002', 'Jane Smith', '3rd Year', 'BSCS', 'B', NULL, '2025-08-31 10:31:47', '2025-08-31 10:31:47'),
(5, '2021-0003', 'Mike Johnson', '2nd Year', 'BSIT', 'A', NULL, '2025-08-31 10:31:47', '2025-08-31 10:31:47'),
(6, '2022-0001', 'Sarah Wilson', '3rd Year', 'BSCS', 'C', NULL, '2025-08-31 10:31:47', '2025-08-31 10:31:47'),
(7, '2022-0002', 'David Brown', '4th Year', 'BSIT', 'B', NULL, '2025-08-31 10:31:47', '2025-08-31 10:31:47');

-- --------------------------------------------------------

--
-- Table structure for table `student_offense_counts`
--

CREATE TABLE `student_offense_counts` (
  `id` int(11) NOT NULL,
  `student_id` varchar(50) NOT NULL,
  `current_offense_count` int(11) NOT NULL DEFAULT 0,
  `last_updated` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `student_offense_counts`
--

INSERT INTO `student_offense_counts` (`id`, `student_id`, `current_offense_count`, `last_updated`) VALUES
(2, '220342', 1, '2025-08-31 14:37:38');

-- --------------------------------------------------------

--
-- Stand-in structure for view `student_stats`
-- (See below for the actual view)
--
CREATE TABLE `student_stats` (
`student_id` varchar(50)
,`student_name` varchar(255)
,`year_level` varchar(50)
,`course` varchar(100)
,`section` varchar(50)
,`current_offense_count` int(11)
,`total_violations` bigint(21)
);

-- --------------------------------------------------------

--
-- Table structure for table `student_violation_offense_counts`
--

CREATE TABLE `student_violation_offense_counts` (
  `id` int(11) NOT NULL,
  `student_id` varchar(50) NOT NULL,
  `violation_type` varchar(255) NOT NULL,
  `offense_count` int(11) NOT NULL DEFAULT 0,
  `last_updated` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('admin','guard','teacher') DEFAULT 'guard',
  `rfid` varchar(50) NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password`, `role`, `created_at`, `updated_at`) VALUES
(1, 'Admin', 'admin@aics.edu.ph', '$2y$10$zUNada4kWb9FGDNkphPwme3Af1F1sYtkcDGp4aNz7Pm/t8Z4Deb1q', 'guard', '2025-07-22 09:10:47', '2025-07-22 09:10:47'),
(2, 'Guard01', 'guard1@aics.edu.ph', '$2y$10$3aiIpD9pW8PeDAQf4eAW8.2D5cHyIulcovoS2Qj7YyawOZxra9XkK', 'guard', '2025-07-23 05:19:30', '2025-07-23 05:19:30'),
(3, 'guard1', 'guard@violationsapp.com', 'guard123', 'guard', '2025-08-31 10:31:47', '2025-08-31 10:31:47'),
(4, 'teacher1', 'teacher@violationsapp.com', 'teacher123', 'teacher', '2025-08-31 10:31:47', '2025-08-31 10:31:47'),
(5, 'Joshua', 'joshuapaviabasco@gmail.com', '12345678', 'guard', '2025-08-31 11:32:58', '2025-08-31 11:32:58'),
(6, 'Joshua Pogi', 'Joshuapogi@aics.edu.p', '12345678', 'guard', '2025-08-31 13:18:51', '2025-08-31 13:18:51'),
(7, 'ajJ', 'Joshua Pogi', '12345678', 'guard', '2025-08-31 13:45:20', '2025-08-31 13:45:20');

-- --------------------------------------------------------

--
-- Table structure for table `violations`
--

CREATE TABLE `violations` (
  `id` int(11) NOT NULL,
  `student_id` varchar(50) NOT NULL,
  `student_name` varchar(255) NOT NULL,
  `year_level` varchar(50) NOT NULL,
  `course` varchar(100) NOT NULL,
  `section` varchar(50) NOT NULL,
  `offense_count` int(11) NOT NULL DEFAULT 1,
  `penalty` varchar(100) DEFAULT NULL,
  `recorded_by` varchar(255) NOT NULL,
  `recorded_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `acknowledged` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `violations`
--

INSERT INTO `violations` (`id`, `student_id`, `student_name`, `year_level`, `course`, `section`, `offense_count`, `penalty`, `recorded_by`, `recorded_at`, `acknowledged`) VALUES
(66, '220342', 'Joshua P. Basco', '4th Year', 'BSCS', 'BS7MA', 1, 'Warning', 'ajJ', '2025-08-31 14:37:38', 0);

-- --------------------------------------------------------

--
-- Table structure for table `violation_details`
--

CREATE TABLE `violation_details` (
  `id` int(11) NOT NULL,
  `violation_id` int(11) NOT NULL,
  `violation_type` varchar(255) NOT NULL,
  `violation_description` text DEFAULT NULL,
  `message_subject` varchar(255) DEFAULT NULL,
  `message_body` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `violation_details`
--

INSERT INTO `violation_details` (`id`, `violation_id`, `violation_type`, `violation_description`, `message_subject`, `message_body`) VALUES
(316, 66, 'No ID', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Stand-in structure for view `violation_summary`
-- (See below for the actual view)
--
CREATE TABLE `violation_summary` (
`id` int(11)
,`student_id` varchar(50)
,`student_name` varchar(255)
,`offense_count` int(11)
,`recorded_by` varchar(255)
,`recorded_at` timestamp
,`violations` mediumtext
);

-- --------------------------------------------------------

--
-- Table structure for table `violation_types`
--

CREATE TABLE `violation_types` (
  `id` int(11) NOT NULL,
  `violation_name` varchar(255) NOT NULL,
  `category` varchar(100) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `violation_types`
--

INSERT INTO `violation_types` (`id`, `violation_name`, `category`, `is_active`, `created_at`) VALUES
(1, 'No ID', 'Dress Code', 1, '2025-07-22 06:23:33'),
(2, 'Wearing of rubber slippers', 'Dress Code', 1, '2025-07-22 06:23:33'),
(3, 'Improper wearing of uniform', 'Dress Code', 1, '2025-07-22 06:23:33'),
(4, 'Non-prescribed haircut', 'Dress Code', 1, '2025-07-22 06:23:33'),
(5, 'Wearing of earrings', 'Dress Code', 1, '2025-07-22 06:23:33'),
(6, 'Wearing of multiple earrings', 'Dress Code', 1, '2025-07-22 06:23:33'),
(7, 'Using cellphones/ gadgets during class hours.', 'Minor', 1, '2025-07-22 06:23:33'),
(8, 'Eating inside the laboratories', 'Minor', 1, '2025-07-22 06:23:33'),
(9, 'Improper not wearing/ tampering of ID', 'Minor', 1, '2025-07-22 06:23:33'),
(10, 'Improper/tampered ID', 'Minor', 1, '2025-07-22 06:23:33'),
(11, 'Improper hairstyle', 'Minor', 1, '2025-07-22 06:23:33'),
(12, 'Improper Uniform', 'Minor', 1, '2025-07-22 06:23:33'),
(13, 'Stealing', 'Major', 1, '2025-07-22 06:23:33'),
(14, 'Vandalism', 'Major', 1, '2025-07-22 06:23:33'),
(15, 'Verbal assault', 'Major', 1, '2025-07-22 06:23:33'),
(16, 'Organizing, planning or joining to any group or fraternity activity.', 'Major', 1, '2025-07-22 06:23:33'),
(17, 'Organizing/joining fraternity activities', 'Major', 1, '2025-07-22 06:23:33'),
(18, 'Cutting Classes', 'Conduct', 1, '2025-07-22 06:23:33'),
(19, 'Cheating/Academic Dishonesty', 'Conduct', 1, '2025-07-22 06:23:33'),
(20, 'Cheating / Academic Dishonesty', 'Conduct', 1, '2025-07-22 06:23:33'),
(21, 'Theft/Stealing', 'Conduct', 1, '2025-07-22 06:23:33'),
(22, 'Theft / Stealing', 'Conduct', 1, '2025-07-22 06:23:33'),
(23, 'Inflicting/Direct Assault', 'Conduct', 1, '2025-07-22 06:23:33'),
(24, 'Inflicting / Direct Assault', 'Conduct', 1, '2025-07-22 06:23:33'),
(25, 'Gambling', 'Conduct', 1, '2025-07-22 06:23:33'),
(26, 'Smoking within the school vicinity', 'Conduct', 1, '2025-07-22 06:23:33'),
(27, 'Smoking within the school', 'Conduct', 1, '2025-07-22 06:23:33'),
(28, 'Possession/Use of Prohibited Drugs', 'Conduct', 1, '2025-07-22 06:23:33'),
(29, 'Use/Possession of Prohibited Drugs', 'Conduct', 1, '2025-07-22 06:23:33'),
(30, 'Possession/Use of Liquor/Alcoholic Beverages', 'Conduct', 1, '2025-07-22 06:23:33'),
(31, 'Use/Possession of Liquor/Alcohol', 'Conduct', 1, '2025-07-22 06:23:33'),
(32, 'Others', 'Miscellaneous', 1, '2025-07-22 06:23:33'),
(33, 'Wearing of earring', 'Dress Code', 1, '2025-08-31 10:31:47'),
(34, 'Using cellphones/ gadgets during class hours', 'Minor', 1, '2025-08-31 10:31:47'),
(35, 'Organizing, planning or joining to any group or fraternity activity', 'Major', 1, '2025-08-31 10:31:47');

-- --------------------------------------------------------

--
-- Structure for view `student_stats`
--
DROP TABLE IF EXISTS `student_stats`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `student_stats`  AS SELECT `s`.`student_id` AS `student_id`, `s`.`student_name` AS `student_name`, `s`.`year_level` AS `year_level`, `s`.`course` AS `course`, `s`.`section` AS `section`, coalesce(`soc`.`current_offense_count`,0) AS `current_offense_count`, count(`v`.`id`) AS `total_violations` FROM ((`students` `s` left join `student_offense_counts` `soc` on(`s`.`student_id` = `soc`.`student_id`)) left join `violations` `v` on(`s`.`student_id` = `v`.`student_id`)) GROUP BY `s`.`student_id` ;

-- --------------------------------------------------------

--
-- Structure for view `violation_summary`
--
DROP TABLE IF EXISTS `violation_summary`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `violation_summary`  AS SELECT `v`.`id` AS `id`, `v`.`student_id` AS `student_id`, `v`.`student_name` AS `student_name`, `v`.`offense_count` AS `offense_count`, `v`.`recorded_by` AS `recorded_by`, `v`.`recorded_at` AS `recorded_at`, group_concat(`vd`.`violation_type` separator ', ') AS `violations` FROM (`violations` `v` left join `violation_details` `vd` on(`v`.`id` = `vd`.`violation_id`)) GROUP BY `v`.`id` ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `penalty_matrix`
--
ALTER TABLE `penalty_matrix`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `violation_type` (`violation_type`,`offense_count`);

--
-- Indexes for table `students`
--
ALTER TABLE `students`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `student_id` (`student_id`),
  ADD KEY `idx_student_id` (`student_id`),
  ADD KEY `idx_student_name` (`student_name`);

--
-- Indexes for table `student_offense_counts`
--
ALTER TABLE `student_offense_counts`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `student_id` (`student_id`),
  ADD KEY `idx_student_id` (`student_id`);

--
-- Indexes for table `student_violation_offense_counts`
--
ALTER TABLE `student_violation_offense_counts`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `student_id` (`student_id`,`violation_type`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD INDEX `idx_rfid` (`rfid`);

--
-- Indexes for table `violations`
--
ALTER TABLE `violations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_student_id` (`student_id`),
  ADD KEY `idx_recorded_at` (`recorded_at`);

--
-- Indexes for table `violation_details`
--
ALTER TABLE `violation_details`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_violation_id` (`violation_id`);

--
-- Indexes for table `violation_types`
--
ALTER TABLE `violation_types`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `violation_name` (`violation_name`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `penalty_matrix`
--
ALTER TABLE `penalty_matrix`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `students`
--
ALTER TABLE `students`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `student_offense_counts`
--
ALTER TABLE `student_offense_counts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `student_violation_offense_counts`
--
ALTER TABLE `student_violation_offense_counts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=291;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `violations`
--
ALTER TABLE `violations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=67;

--
-- AUTO_INCREMENT for table `violation_details`
--
ALTER TABLE `violation_details`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=317;

--
-- AUTO_INCREMENT for table `violation_types`
--
ALTER TABLE `violation_types`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `student_offense_counts`
--
ALTER TABLE `student_offense_counts`
  ADD CONSTRAINT `student_offense_counts_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE CASCADE;

--
-- Constraints for table `violations`
--
ALTER TABLE `violations`
  ADD CONSTRAINT `violations_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`) ON DELETE CASCADE;

--
-- Constraints for table `violation_details`
--
ALTER TABLE `violation_details`
  ADD CONSTRAINT `violation_details_ibfk_1` FOREIGN KEY (`violation_id`) REFERENCES `violations` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
