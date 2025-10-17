// File: ProductMenu.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProductMenu extends JFrame {
    public static void main(String[] args) {
        ProductMenu menu = new ProductMenu();

        menu.setSize(850, 600);
        menu.setLocationRelativeTo(null);
        menu.setContentPane(menu.mainPanel);
        menu.getContentPane().setBackground(Color.white);
        menu.setVisible(true);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private int selectedIndex = -1;
    private ArrayList<Product> listProduct;
    private Database database;

    private JPanel mainPanel;
    private JTextField idField;
    private JTextField namaField;
    private JTextField hargaField;
    private JTable productTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox<String> kategoriComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel idLabel;
    private JLabel namaLabel;
    private JLabel hargaLabel;
    private JLabel kategoriLabel;

    private JLabel tribunLabel;
    private JComboBox<String> tribunComboBox;

    public ProductMenu() {
        listProduct = new ArrayList<>();

        database = new Database();


        // isi tabel
        productTable.setModel(setTable());

        // ubah teks label
        titleLabel.setText("TIKET MOTOGP");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        idLabel.setText("ID Tiket:");
        namaLabel.setText("Sirkuit:");
        kategoriLabel.setText("Jenis Tiket:");
        hargaLabel.setText("Harga (Rp):");

        // isi combo box kategori tiket
        String[] kategoriData = {
                "--- Pilih Jenis Tiket ---",
                "Day 1 (Practice)",
                "Day 2 (Qualifications)",
                "Day 3 (Race Day)",
                "3 days full"
        };
        kategoriComboBox.setModel(new DefaultComboBoxModel<>(kategoriData));

        // isi combo box zona tribun
        tribunLabel = new JLabel("Zona Tribun:");
        String[] tribunData = {
                "--- Pilih Zona Tribun ---",
                "Grandstand",
                "Premium Grandstand",
                "VIP"
        };
        tribunComboBox.setModel(new DefaultComboBoxModel<>(tribunData));

        // sembunyikan tombol delete awalnya
        deleteButton.setVisible(false);

        // tombol Add/Update
        addUpdateButton.addActionListener(e -> {
            if (selectedIndex == -1) {
                insertData();
            } else {
                updateData();
            }
        });

        // tombol Delete
        deleteButton.addActionListener(e -> {
            int dialogResult = JOptionPane.showConfirmDialog(
                    null,
                    "Yakin ingin menghapus data tiket ini?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (dialogResult == JOptionPane.YES_OPTION) {
                deleteData();
            }
        });

        // tombol Cancel
        cancelButton.addActionListener(e -> clearForm());

        // klik tabel
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedIndex = productTable.getSelectedRow();

                String curID = productTable.getModel().getValueAt(selectedIndex, 1).toString();
                String curNama = productTable.getModel().getValueAt(selectedIndex, 2).toString();
                String curHargaString = productTable.getModel().getValueAt(selectedIndex, 3).toString()
                        .replaceAll("[^0-9.]", "")
                        .replace(",", "")
                        .trim();
                String curKategori = productTable.getModel().getValueAt(selectedIndex, 4).toString();
                String curTribun = productTable.getModel().getValueAt(selectedIndex, 5).toString();

                idField.setText(curID);
                namaField.setText(curNama);
                hargaField.setText(curHargaString);
                kategoriComboBox.setSelectedItem(curKategori);
                tribunComboBox.setSelectedItem(curTribun);

                addUpdateButton.setText("Update");
                deleteButton.setVisible(true);
            }
        });
    }

    // ambil tribun dari combo box
    private String getSelectedTribun() {
        String selected = (String) tribunComboBox.getSelectedItem();
        if (selected == null || selected.contains("---")) return null;
        return selected;
    }

    // set tabel
    public final DefaultTableModel setTable() {
        Object[] cols = {"No", "ID Tiket", "Sirkuit", "Harga", "Jenis Tiket", "Zona Tribun"};
        DefaultTableModel tmp = new DefaultTableModel(null, cols);

        try {
            ResultSet resultSet = database.selectQuery("SELECT * FROM product");
            //isi tabel dengan hasil query
            int i = 0;
            while (resultSet.next()) {
                Object[] row = new Object[6];
                row[0] = i+1;
                row[1] = resultSet.getString("id");
                row[2] = resultSet.getString("nama");
                row[3] = resultSet.getString("harga");
                row[4] = resultSet.getString("kategori");
                row[5] = resultSet.getString("tribun");
                tmp.addRow(row);
                i++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tmp;
    }
    private boolean isFormValid() {
        if (idField.getText().trim().isEmpty() ||
                namaField.getText().trim().isEmpty() ||
                hargaField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(null, "Semua kolom harus diisi!", "Validasi Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // tambah data
    public void insertData() {
        if (!isFormValid()) {
            return;
        }

        try {
            String id = idField.getText();
            String nama = namaField.getText(); // circuit
            double harga = Double.parseDouble(hargaField.getText());
            String kategori = (String) kategoriComboBox.getSelectedItem();
            String tribun = getSelectedTribun();

            // 1. Lakukan Validasi
            if (kategori == null || kategori.contains("---")) {
                JOptionPane.showMessageDialog(null, "Pilih Jenis Tiket!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (tribun == null) {
                JOptionPane.showMessageDialog(null, "Pilih Zona Tribun!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Jalankan Query ke Database
            // Perhatikan penggunaan format string yang aman atau PreparedStatement untuk menghindari SQL Injection
            String sqlQuery = "INSERT INTO product (id, nama, harga, kategori, tribun) VALUES ('" + id + "','" + nama + "'," + harga + ", '" + kategori + "', '" + tribun + "')";
            database.insertUpdateDeleteQuery(sqlQuery);

            // 3. Update UI
            productTable.setModel(setTable());
            clearForm();
            JOptionPane.showMessageDialog(null, "Data Tiket berhasil ditambah", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Harga harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            String errorMessage = ex.getMessage();

            // Cek apakah pesan error mengandung indikasi 'Duplicate entry' (Primary Key violation)
            if (errorMessage != null && (errorMessage.contains("Duplicate entry") || errorMessage.contains("PRIMARY"))) {
                // PESAN KHUSUS UNTUK ID DUPLIKAT
                JOptionPane.showMessageDialog(
                        null,
                        "ID tiket telah digunakan",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } else {
                // Pesan error default untuk error lainnya
                JOptionPane.showMessageDialog(
                        null,
                        "Gagal menambah data ke database: " + errorMessage,
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }        }
    }

    // update data
    public void updateData() {
        if (!isFormValid()) {
            return;
        }
        try {
            String id = idField.getText();
            String nama = namaField.getText();
            double harga = Double.parseDouble(hargaField.getText());
            String kategori = (String) kategoriComboBox.getSelectedItem();
            String tribun = getSelectedTribun();

            // 1. Lakukan Validasi
            if (kategori == null || kategori.contains("---")) {
                JOptionPane.showMessageDialog(null, "Pilih Jenis Tiket!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (tribun == null) {
                JOptionPane.showMessageDialog(null, "Pilih Zona Tribun!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Jalankan Query UPDATE ke Database
            String sqlQuery = "UPDATE product SET nama = '" + nama + "', harga = " + harga + ", kategori = '" + kategori + "', tribun = '" + tribun + "' WHERE id = '" + id + "'";
            database.insertUpdateDeleteQuery(sqlQuery);

            // 3. Update UI
            productTable.setModel(setTable());
            clearForm();
            JOptionPane.showMessageDialog(null, "Data Tiket berhasil diubah", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Harga harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Gagal mengubah data di database: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteData() {
        // Ambil ID dari field, karena ini adalah primary key yang digunakan untuk menghapus
        String idToDelete = idField.getText();

        try {
            // Tambahkan validasi index/ID jika diperlukan, tetapi jika form diisi dari tabel, ID seharusnya ada.
            if (idToDelete.isEmpty()) {
                JOptionPane.showMessageDialog(null, "ID Tiket tidak ditemukan untuk dihapus.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 1. Jalankan Query DELETE ke Database
            String sqlQuery = "DELETE FROM product WHERE id = '" + idToDelete + "'";
            database.insertUpdateDeleteQuery(sqlQuery);

            // 2. Update UI
            productTable.setModel(setTable());
            clearForm();
            JOptionPane.showMessageDialog(null, "Data Tiket berhasil dihapus");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Gagal menghapus data dari database: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearForm() {
        idField.setText("");
        namaField.setText("");
        hargaField.setText("");
        kategoriComboBox.setSelectedIndex(0);
        tribunComboBox.setSelectedIndex(0);
        addUpdateButton.setText("Add");
        deleteButton.setVisible(false);
        selectedIndex = -1;
    }
}
