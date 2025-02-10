package com.mycompany.fin.exam;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RentalApplication extends JFrame {
    private JButton btnLogin;
    private JButton btnRegister;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblUsername;
    private JLabel lblPassword;
    
    public RentalApplication() {
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Vehicle Rental System");
        setSize(400, 300);
        setLayout(null);
        
        lblUsername = new JLabel("Username:");
        lblUsername.setBounds(50, 50, 80, 25);
        
        txtUsername = new JTextField();
        txtUsername.setBounds(140, 50, 200, 25);
        
        lblPassword = new JLabel("Password:");
        lblPassword.setBounds(50, 90, 80, 25);
        
        txtPassword = new JPasswordField();
        txtPassword.setBounds(140, 90, 200, 25);
        
        btnLogin = new JButton("Login");
        btnLogin.setBounds(140, 130, 90, 25);
        
        btnRegister = new JButton("Register");
        btnRegister.setBounds(250, 130, 90, 25);
        
        add(lblUsername);
        add(txtUsername);
        add(lblPassword);
        add(txtPassword);
        add(btnLogin);
        add(btnRegister);
        
        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> openRegistration());
    }
    
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean isAdmin = rs.getBoolean("is_admin");
                int userId = rs.getInt("id");
                
                if (isAdmin) {
                    VehicleManagementForm adminForm = new VehicleManagementForm();
                    adminForm.setVisible(true);
                } else {
                    RentalForm rentalForm = new RentalForm(userId);
                    rentalForm.setVisible(true);
                }
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }
    
    private void openRegistration() {
        RegistrationForm regForm = new RegistrationForm();
        regForm.setVisible(true);
        this.dispose();
    }
    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new RentalApplication().setVisible(true);
        });
    }
}