package com.mycompany.fin.exam;

import javax.swing.*;
import java.sql.*;

public class RegistrationForm extends JFrame {
    private JTextField txtName;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnRegister;
    private JButton btnBack;
    
    public RegistrationForm() {
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Registration");
        setSize(400, 300);
        setLayout(null);
        
        JLabel lblName = new JLabel("Name:");
        lblName.setBounds(50, 50, 80, 25);
        
        txtName = new JTextField();
        txtName.setBounds(140, 50, 200, 25);
        
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setBounds(50, 90, 80, 25);
        
        txtUsername = new JTextField();
        txtUsername.setBounds(140, 90, 200, 25);
        
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(50, 130, 80, 25);
        
        txtPassword = new JPasswordField();
        txtPassword.setBounds(140, 130, 200, 25);
        
        btnRegister = new JButton("Register");
        btnRegister.setBounds(140, 170, 90, 25);
        
        btnBack = new JButton("Back");
        btnBack.setBounds(250, 170, 90, 25);
        
        add(lblName);
        add(txtName);
        add(lblUsername);
        add(txtUsername);
        add(lblPassword);
        add(txtPassword);
        add(btnRegister);
        add(btnBack);
        
        btnRegister.addActionListener(e -> handleRegistration());
        btnBack.addActionListener(e -> backToLogin());
    }
    
    private void handleRegistration() {
        String name = txtName.getText();
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        
        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (name, username, password) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, password);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Registration successful!");
                backToLogin();
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage());
            }
        }
    }
    
    private void backToLogin() {
        new RentalApplication().setVisible(true);
        this.dispose();
    }
}