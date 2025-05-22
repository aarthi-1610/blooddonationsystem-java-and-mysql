package bloodbank;

import java.sql.*;
import java.util.Scanner;

public class DonorApp {
    static final String DB_URL = "jdbc:mysql://localhost:3306/blood_bank";
    static final String USER = "root";
    static final String PASS = "";  // your MySQL password

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Scanner sc = new Scanner(System.in)) {

            System.out.println("✅ Connected to blood_bank database.");

            while (true) {
                System.out.println("\n===== Blood Donation Management =====");
                System.out.println("1. Manage Donors");
                System.out.println("2. Manage Recipients");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");
                int choice = sc.nextInt();
                sc.nextLine();  // consume newline

                if (choice == 1) {
                    manageDonors(conn, sc);
                } else if (choice == 2) {
                    manageRecipients(conn, sc);
                } else if (choice == 3) {
                    System.out.println("Exiting... Thank you!");
                    break;
                } else {
                    System.out.println("❌ Invalid choice. Please try again.");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ SQL Error: " + e.getMessage());
        }
    }

    // Donor management
    private static void manageDonors(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n--- Donor Registration ---");
        System.out.print("Enter donor name: ");
        String name = sc.nextLine();

        System.out.print("Enter age: ");
        int age = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter blood group: ");
        String bloodGroup = sc.nextLine();

        System.out.print("Enter city: ");
        String city = sc.nextLine();

        System.out.print("Enter phone number: ");
        String phone = sc.nextLine();

        String insertSQL = "INSERT INTO donors (name, age, blood_group, city, phone) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, bloodGroup);
            pstmt.setString(4, city);
            pstmt.setString(5, phone);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Donor added successfully!");
            }
        }

        displayDonors(conn);
    }

    // Recipient management with blood group availability check
    private static void manageRecipients(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n--- Recipient Registration ---");
        System.out.print("Enter recipient name: ");
        String name = sc.nextLine();

        System.out.print("Enter required blood group: ");
        String requiredBloodGroup = sc.nextLine();

        System.out.print("Enter city: ");
        String city = sc.nextLine();

        System.out.print("Enter phone number: ");
        String phone = sc.nextLine();

        // Check if donor with required blood group exists
        String checkSQL = "SELECT COUNT(*) AS count FROM donors WHERE blood_group = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setString(1, requiredBloodGroup);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                if (count == 0) {
                    System.out.println("❌ Sorry, no donors with blood group " + requiredBloodGroup + " are available currently.");
                    return;  // exit without inserting recipient
                }
            }
        }

        // If donor exists, insert recipient
        String insertSQL = "INSERT INTO recipients (name, required_blood_group, city, phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, requiredBloodGroup);
            pstmt.setString(3, city);
            pstmt.setString(4, phone);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Recipient added successfully!");
            }
        }

        displayRecipients(conn);
    }

    // Display all donors
    private static void displayDonors(Connection conn) throws SQLException {
        System.out.println("\n📋 List of Donors:");
        String sql = "SELECT * FROM donors";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        ", Name: " + rs.getString("name") +
                        ", Age: " + rs.getInt("age") +
                        ", Blood Group: " + rs.getString("blood_group") +
                        ", City: " + rs.getString("city") +
                        ", Phone: " + rs.getString("phone"));
            }
        }
    }

    // Display all recipients
    private static void displayRecipients(Connection conn) throws SQLException {
        System.out.println("\n📋 List of Recipients:");
        String sql = "SELECT * FROM recipients";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        ", Name: " + rs.getString("name") +
                        ", Required Blood Group: " + rs.getString("required_blood_group") +
                        ", City: " + rs.getString("city") +
                        ", Phone: " + rs.getString("phone"));
            }
        }
    }
}
