package quanlychitieu;

import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.sql.*;

public class Ungdung extends JFrame {

    JTable table;
    DefaultTableModel model;

    JTextField tfTien, tfDanhmuc, tfNgay, tfGhichu, tfTimkiem;
    JComboBox<String> cbLoai, cbBoloc;

    JLabel lbTongThu, lbTongChi, lbSoDu;

    Connection conn;

    int id = 0; // ID sẽ lấy từ database

    public Ungdung() {
        setTitle("Quản lý chi tiêu cá nhân");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        connectDB();      // Kết nối MySQL
        initComponents(); // Tạo GUI
        loadDataFromDB(); // Load dữ liệu từ database
    }

    // ======== KẾT NỐI DATABASE =========
    void connectDB() {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/quanlychitieu?useSSL=false&serverTimezone=UTC",
                    "root", "244206" // đổi user/password nếu khác
            );
            System.out.println("Kết nối database thành công!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không kết nối được database!");
        }
    }

    // ======== TẠO GIAO DIỆN =========
    void initComponents() {
        // ======== MENU BAR =========
        JMenuBar menubar = new JMenuBar();
        JMenu menuFile = new JMenu("Tệp");
        JMenuItem mThoat = new JMenuItem("Thoát");
        mThoat.addActionListener(e -> System.exit(0));
        menuFile.add(mThoat);

        JMenu menuTroGiup = new JMenu("Thông tin");
        JMenuItem mAbout = new JMenuItem("Nhóm tác giả");
        mAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Ứng dụng quản lý chi tiêu\nTác giả: Bạn"));
        menuTroGiup.add(mAbout);

        menubar.add(menuFile);
        menubar.add(menuTroGiup);
        setJMenuBar(menubar);

        // ======== PANEL NHẬP DỮ LIỆU =========
        JPanel pNhap = new JPanel(new GridLayout(2, 5, 5, 5));
        pNhap.setBorder(BorderFactory.createTitledBorder("Nhập chi tiêu"));

        tfTien = new JTextField();
        cbLoai = new JComboBox<>(new String[]{"Chi", "Thu"});
        tfDanhmuc = new JTextField();
        tfNgay = new JTextField(LocalDate.now().toString());
        tfGhichu = new JTextField();

        pNhap.add(new JLabel("Số tiền:"));
        pNhap.add(new JLabel("Loại:"));
        pNhap.add(new JLabel("Danh mục:"));
        pNhap.add(new JLabel("Ngày:"));
        pNhap.add(new JLabel("Ghi chú:"));

        pNhap.add(tfTien);
        pNhap.add(cbLoai);
        pNhap.add(tfDanhmuc);
        pNhap.add(tfNgay);
        pNhap.add(tfGhichu);

        // ======== PANEL CHỨC NĂNG =========
        JPanel pChucNang = new JPanel(new GridLayout(5, 1, 5, 5));
        pChucNang.setBorder(BorderFactory.createTitledBorder("Chức năng"));

        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnClear = new JButton("Làm mới");

        pChucNang.add(btnThem);
        pChucNang.add(btnSua);
        pChucNang.add(btnXoa);
        pChucNang.add(btnClear);

        // ======== PANEL TÌM KIẾM =========
        JPanel pSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pSearch.setBorder(BorderFactory.createTitledBorder("Tìm kiếm & Lọc"));

        tfTimkiem = new JTextField(20);
        cbBoloc = new JComboBox<>(new String[]{"Tất cả", "Thu", "Chi"});

        pSearch.add(new JLabel("Tìm kiếm tên/danh mục: "));
        pSearch.add(tfTimkiem);
        pSearch.add(new JLabel("Lọc loại: "));
        pSearch.add(cbBoloc);

        // ======== TABLE HIỂN THỊ =========
        String[] col = {"ID", "Số tiền", "Loại", "Danh mục", "Ngày", "Ghi chú"};
        model = new DefaultTableModel(col, 0);
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        // ======== PANEL THỐNG KÊ =========
        JPanel pThongKe = new JPanel(new GridLayout(1, 3));
        pThongKe.setBorder(BorderFactory.createTitledBorder("Thống kê"));

        lbTongThu = new JLabel("Tổng thu: 0");
        lbTongChi = new JLabel("Tổng chi: 0");
        lbSoDu = new JLabel("Số dư: 0");

        pThongKe.add(lbTongThu);
        pThongKe.add(lbTongChi);
        pThongKe.add(lbSoDu);

        // ======== GHÉP VÀO FRAME =========
        add(pNhap, BorderLayout.NORTH);
        add(pChucNang, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(pSearch, BorderLayout.NORTH);
        centerPanel.add(sp, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        add(pThongKe, BorderLayout.SOUTH);

        // ======== SỰ KIỆN NÚT =========
        btnThem.addActionListener(e -> addRow());
        btnXoa.addActionListener(e -> deleteRow());
        btnClear.addActionListener(e -> clearAllData());
        btnSua.addActionListener(e -> editRow());

        tfTimkiem.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { search(); }
        });
        cbBoloc.addActionListener(e -> filter());

        setVisible(true);
    }

    // ======== CÁC HÀM CHỨC NĂNG =========
    String dinhDangTien(String s) {
        try {
            long so = Long.parseLong(s);
            DecimalFormat df = new DecimalFormat("#,###");
            return df.format(so).replace(",", ".");
        } catch (Exception e) { return s; }
    }

    void addRow() {
        try {
            String tien = tfTien.getText();
            String loai = cbLoai.getSelectedItem().toString();
            String danhmuc = tfDanhmuc.getText();
            String ngay = tfNgay.getText();
            String ghichu = tfGhichu.getText();

            // Thêm vào database
            String sql = "INSERT INTO chi_tieu (sotien, loai, danhmuc, ngay, ghichu) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setLong(1, Long.parseLong(tien));
            pst.setString(2, loai);
            pst.setString(3, danhmuc);
            pst.setDate(4, Date.valueOf(ngay));
            pst.setString(5, ghichu);
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) id = rs.getInt(1);

            String[] row = { String.valueOf(id), dinhDangTien(tien), loai, danhmuc, ngay, ghichu };
            model.addRow(row);

            updateStats();
            clearInput();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi nhập dữ liệu!");
        }
    }

    void editRow() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        try {
            int rowId = Integer.parseInt(model.getValueAt(r, 0).toString());
            String tien = tfTien.getText();
            String loai = cbLoai.getSelectedItem().toString();
            String danhmuc = tfDanhmuc.getText();
            String ngay = tfNgay.getText();
            String ghichu = tfGhichu.getText();

            String sql = "UPDATE chi_tieu SET sotien=?, loai=?, danhmuc=?, ngay=?, ghichu=? WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setLong(1, Long.parseLong(tien));
            pst.setString(2, loai);
            pst.setString(3, danhmuc);
            pst.setDate(4, Date.valueOf(ngay));
            pst.setString(5, ghichu);
            pst.setInt(6, rowId);
            pst.executeUpdate();

            model.setValueAt(dinhDangTien(tien), r, 1);
            model.setValueAt(loai, r, 2);
            model.setValueAt(danhmuc, r, 3);
            model.setValueAt(ngay, r, 4);
            model.setValueAt(ghichu, r, 5);

            updateStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
void clearAllData() {
    int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa toàn bộ dữ liệu trong bảng?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            // Xóa toàn bộ dữ liệu trong database
            Statement st = conn.createStatement();
            st.executeUpdate("DELETE FROM chi_tieu");

            // Xóa toàn bộ dữ liệu trên JTable
            model.setRowCount(0);

            // Reset ID cho lần thêm tiếp theo
            id = 1;

            // Cập nhật lại thống kê
            updateStats();

            JOptionPane.showMessageDialog(this, "Đã xóa toàn bộ dữ liệu trong bảng!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa dữ liệu!");
        }
    }
}

    void deleteRow() {
        int r = table.getSelectedRow();
        if (r >= 0) {
            try {
                int rowId = Integer.parseInt(model.getValueAt(r, 0).toString());
                String sql = "DELETE FROM chi_tieu WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, rowId);
                pst.executeUpdate();

                model.removeRow(r);
                updateStats();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    void clearInput() {
        tfTien.setText("");
        tfDanhmuc.setText("");
        tfGhichu.setText("");
        tfNgay.setText(LocalDate.now().toString());
        cbLoai.setSelectedIndex(0);
    }

    void search() {
        String key = tfTimkiem.getText().toLowerCase();
        for (int i = 0; i < table.getRowCount(); i++) {
            String dm = table.getValueAt(i, 3).toString().toLowerCase();
            String gc = table.getValueAt(i, 5).toString().toLowerCase();
            boolean match = dm.contains(key) || gc.contains(key);
            table.setRowHeight(i, match ? 22 : 0);
        }
    }

    void filter() {
        String loai = cbBoloc.getSelectedItem().toString();
        for (int i = 0; i < table.getRowCount(); i++) {
            String rowLoai = table.getValueAt(i, 2).toString();
            boolean show = loai.equals("Tất cả") || rowLoai.equals(loai);
            table.setRowHeight(i, show ? 22 : 0);
        }
    }

    void updateStats() {
        int tongThu = 0, tongChi = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            int tien = Integer.parseInt(model.getValueAt(i, 1).toString().replace(".", ""));
            String loai = model.getValueAt(i, 2).toString();
            if (loai.equals("Thu")) tongThu += tien;
            else tongChi += tien;
        }
        lbTongThu.setText("Tổng thu: " + tongThu);
        lbTongChi.setText("Tổng chi: " + tongChi);
        lbSoDu.setText("Số dư: " + (tongThu - tongChi));
    }

    // ======== LOAD DỮ LIỆU TỪ DATABASE =========
    void loadDataFromDB() {
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM chi_tieu");
            while (rs.next()) {
                int rowId = rs.getInt("id");
                String tien = dinhDangTien(String.valueOf(rs.getLong("sotien")));
                String loai = rs.getString("loai");
                String danhmuc = rs.getString("danhmuc");
                String ngay = rs.getDate("ngay").toString();
                String ghichu = rs.getString("ghichu");
                String[] row = { String.valueOf(rowId), tien, loai, danhmuc, ngay, ghichu };
                model.addRow(row);
                id = Math.max(id, rowId); // cập nhật id
            }
            id++; // id tiếp theo
            updateStats();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ungdung());
    }
}
