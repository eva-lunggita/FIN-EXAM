package com.mycompany.fin.exam;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class VehicleManagementForm extends JFrame {
    private JTable tblVehicles;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnLogout;
    private DefaultTableModel tableModel;
    
    public VehicleManagementForm() {
        initComponents();
        loadVehicles();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Vehicle Management");
        setSize(1000, 600); 
        setLayout(null);
        
        // Create table model with additional columns for renter info
        tableModel = new DefaultTableModel(
            new String[] {"ID", "Name", "Type", "Status", "Rented By", "Rental Date"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblVehicles = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tblVehicles);
        scrollPane.setBounds(20, 20, 940, 400); 
        
        btnAdd = new JButton("Add Vehicle");
        btnAdd.setBounds(20, 440, 120, 30);
        
        btnEdit = new JButton("Edit Vehicle");
        btnEdit.setBounds(160, 440, 120, 30);
        
        btnDelete = new JButton("Delete Vehicle");
        btnDelete.setBounds(300, 440, 120, 30);
        
        btnLogout = new JButton("Logout");
        btnLogout.setBounds(880, 440, 80, 30); 
        
        add(scrollPane);
        add(btnAdd);
        add(btnEdit);
        add(btnDelete);
        add(btnLogout);
        
        btnAdd.addActionListener(e -> addVehicle());
        btnEdit.addActionListener(e -> editVehicle());
        btnDelete.addActionListener(e -> deleteVehicle());
        btnLogout.addActionListener(e -> logout());
    }
    
    private void loadVehicles() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = 
                "SELECT v.id, v.name, v.type, v.available, " +
                "u.name as renter_name, r.rental_date " +
                "FROM vehicles v " +
                "LEFT JOIN rentals r ON v.id = r.vehicle_id AND r.active = true " +
                "LEFT JOIN users u ON r.user_id = u.id " +
                "ORDER BY v.id";
                
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                boolean available = rs.getBoolean("available");
                String renterName = rs.getString("renter_name");
                Timestamp rentalDate = rs.getTimestamp("rental_date");
                
                tableModel.addRow(new Object[] {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("type"),
                    available ? "Available" : "Rented",
                    renterName != null ? renterName : "-",
                    rentalDate != null ? rentalDate.toString() : "-"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading vehicles: " + ex.getMessage());
        }
    }
    
    private void addVehicle() {
        JTextField txtName = new JTextField();
        JTextField txtType = new JTextField();
        
        Object[] fields = {
            "Vehicle Name:", txtName,
            "Vehicle Type:", txtType
        };
        
        int result = JOptionPane.showConfirmDialog(this, fields, 
            "Add New Vehicle", JOptionPane.OK_CANCEL_OPTION);
            
        if (result == JOptionPane.OK_OPTION) {
            String name = txtName.getText().trim();
            String type = txtType.getText().trim();
            
            if (name.isEmpty() || type.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO vehicles (name, type, available) VALUES (?, ?, true)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, name);
                stmt.setString(2, type);
                
                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Vehicle added successfully!");
                    loadVehicles();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding vehicle: " + ex.getMessage());
            }
        }
    }
    
    private void editVehicle() {
        int selectedRow = tblVehicles.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to edit!");
            return;
        }
        
        int vehicleId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentType = (String) tableModel.getValueAt(selectedRow, 2);
        
        JTextField txtName = new JTextField(currentName);
        JTextField txtType = new JTextField(currentType);
        
        Object[] fields = {
            "Vehicle Name:", txtName,
            "Vehicle Type:", txtType
        };
        
        int result = JOptionPane.showConfirmDialog(this, fields, 
            "Edit Vehicle", JOptionPane.OK_CANCEL_OPTION);
            
        if (result == JOptionPane.OK_OPTION) {
            String name = txtName.getText().trim();
            String type = txtType.getText().trim();
            
            if (name.isEmpty() || type.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE vehicles SET name = ?, type = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, name);
                stmt.setString(2, type);
                stmt.setInt(3, vehicleId);
                
                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Vehicle updated successfully!");
                    loadVehicles();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating vehicle: " + ex.getMessage());
            }
        }
    }
    
    private void deleteVehicle() {
        int selectedRow = tblVehicles.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to delete!");
            return;
        }
        
        int vehicleId = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 3);
        
        if (!status.equals("Available")) {
            JOptionPane.showMessageDialog(this, "Cannot delete a rented vehicle!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this vehicle?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM vehicles WHERE id = ? AND available = true";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, vehicleId);
                
                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, "Vehicle deleted successfully!");
                    loadVehicles();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting vehicle: " + ex.getMessage());
            }
        }
    }
    
    private void logout() {
        new RentalApplication().setVisible(true);
        this.dispose();
    }
}