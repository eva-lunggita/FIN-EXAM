package com.mycompany.fin.exam;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalForm extends JFrame {
    private JTable tblVehicles;
    private JButton btnRent;
    private JButton btnReturn;
    private JButton btnLogout;
    private final int userId;
    private CheckboxTableModel tableModel;
    
    public RentalForm(int userId) {
        this.userId = userId;
        initComponents();
        loadVehicles();
        setLocationRelativeTo(null);
    }
    
    private class CheckboxTableModel extends DefaultTableModel {
        public CheckboxTableModel(String[] columnNames, int rows) {
            super(columnNames, rows);
        }
        
        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return Boolean.class;
            }
            return super.getColumnClass(column);
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 0) {
                String status = (String) getValueAt(row, 4);
                return status.equals("Available");
            }
            return false;
        }
        
        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column == 0) {
                if ((Boolean) value) {
                    int selectedCount = 0;
                    for (int i = 0; i < getRowCount(); i++) {
                        if (i != row && (Boolean) getValueAt(i, 0)) {
                            selectedCount++;
                        }
                    }
                    if (selectedCount >= 2) {
                        JOptionPane.showMessageDialog(null, 
                            "You can only select up to 2 vehicles at once!");
                        return;
                    }
                }
            }
            super.setValueAt(value, row, column);
        }
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Vehicle Rental");
        setSize(800, 600);
        setLayout(null);
        
        tableModel = new CheckboxTableModel(
            new String[] {"", "ID", "Name", "Type", "Status"}, 0
        );
        
        tblVehicles = new JTable(tableModel);
        
        // Add checkbox renderer for the first column
        tblVehicles.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            private final JCheckBox checkbox = new JCheckBox();
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Boolean) {
                    checkbox.setSelected((Boolean) value);
                    checkbox.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    checkbox.setHorizontalAlignment(JLabel.CENTER);
                    return checkbox;
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        
        
        TableColumnModel columnModel = tblVehicles.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(30);  // Checkbox column
        columnModel.getColumn(0).setMinWidth(30);
        columnModel.getColumn(1).setMaxWidth(50);  // ID column
        columnModel.getColumn(1).setMinWidth(50);
        columnModel.getColumn(2).setPreferredWidth(200); // Name column
        columnModel.getColumn(3).setPreferredWidth(100); // Type column
        columnModel.getColumn(4).setPreferredWidth(100); // Status column
        
        JScrollPane scrollPane = new JScrollPane(tblVehicles);
        scrollPane.setBounds(20, 20, 740, 400);
        
        btnRent = new JButton("Rent Selected Vehicles");
        btnRent.setBounds(20, 440, 160, 30);
        
        btnReturn = new JButton("Return Vehicle");
        btnReturn.setBounds(200, 440, 120, 30);
        
        btnLogout = new JButton("Logout");
        btnLogout.setBounds(680, 440, 80, 30);
        
        JLabel lblHelper = new JLabel("* Select up to 2 vehicles to rent at once");
        lblHelper.setBounds(20, 480, 300, 20);
        
        add(scrollPane);
        add(btnRent);
        add(btnReturn);
        add(btnLogout);
        add(lblHelper);
        
        btnRent.addActionListener(e -> rentVehicles());
        btnReturn.addActionListener(e -> returnVehicle());
        btnLogout.addActionListener(e -> logout());
    }
    
    private void loadVehicles() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM vehicles";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                    false,
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getBoolean("available") ? "Available" : "Rented"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading vehicles: " + ex.getMessage());
        }
    }
    
    private void rentVehicles() {
        List<Integer> vehicleIds = new ArrayList<>();
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked) {
                vehicleIds.add((Integer) tableModel.getValueAt(i, 1));
            }
        }
        
        if (vehicleIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select vehicle(s) to rent!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check current rentals
            String checkQuery = "SELECT COUNT(*) FROM rentals WHERE user_id = ? AND active = true";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                int currentRentals = rs.getInt(1);
                if (currentRentals + vehicleIds.size() > 2) {
                    JOptionPane.showMessageDialog(this, 
                        "You can only have a maximum of 2 active rentals at a time!");
                    return;
                }
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            try {
                String rentalQuery = "INSERT INTO rentals (user_id, vehicle_id, rental_date, active) VALUES (?, ?, NOW(), true)";
                String updateQuery = "UPDATE vehicles SET available = false WHERE id = ?";
                
                PreparedStatement rentalStmt = conn.prepareStatement(rentalQuery);
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                
                for (int vehicleId : vehicleIds) {
                    rentalStmt.setInt(1, userId);
                    rentalStmt.setInt(2, vehicleId);
                    rentalStmt.executeUpdate();
                    
                    updateStmt.setInt(1, vehicleId);
                    updateStmt.executeUpdate();
                }
                
                conn.commit();
                JOptionPane.showMessageDialog(this, "Vehicles rented successfully!");
                loadVehicles();
                
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error renting vehicles: " + ex.getMessage());
        }
    }
    
    private void returnVehicle() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT r.id, v.name FROM rentals r " +
                          "JOIN vehicles v ON r.vehicle_id = v.id " +
                          "WHERE r.user_id = ? AND r.active = true";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            while (rs.next()) {
                model.addElement(rs.getInt("id") + " - " + rs.getString("name"));
            }
            
            if (model.getSize() == 0) {
                JOptionPane.showMessageDialog(this, "You have no active rentals!");
                return;
            }
            
            JComboBox<String> rentalCombo = new JComboBox<>(model);
            int result = JOptionPane.showConfirmDialog(this, rentalCombo, 
                "Select vehicle to return", JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                String selected = (String) rentalCombo.getSelectedItem();
                int rentalId = Integer.parseInt(selected.split(" - ")[0]);
                
                conn.setAutoCommit(false);
                try {
                    String updateRental = "UPDATE rentals SET active = false, return_date = NOW() WHERE id = ?";
                    PreparedStatement updateRentalStmt = conn.prepareStatement(updateRental);
                    updateRentalStmt.setInt(1, rentalId);
                    updateRentalStmt.executeUpdate();
                    
                    String updateVehicle = "UPDATE vehicles v JOIN rentals r ON v.id = r.vehicle_id " +
                                         "SET v.available = true WHERE r.id = ?";
                    PreparedStatement updateVehicleStmt = conn.prepareStatement(updateVehicle);
                    updateVehicleStmt.setInt(1, rentalId);
                    updateVehicleStmt.executeUpdate();
                    
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Vehicle returned successfully!");
                    loadVehicles();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error returning vehicle: " + ex.getMessage());
        }
    }
    
    private void logout() {
        new RentalApplication().setVisible(true);
        this.dispose();
    }
}