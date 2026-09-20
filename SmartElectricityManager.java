// Smart Electricity Bill Manager - FINAL WITH LARGER FONTS (Whole System), Tab Font Slightly Reduced
// Save as: SmartElectricityManager.java
// Compile: javac -cp ".:lib/itextpdf-5.5.13.2.jar" SmartElectricityManager.java
// Run:     java -cp ".:lib/itextpdf-5.5.13.2.jar" SmartElectricityManager

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;

// iText imports
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;

public class SmartElectricityManager {
    public static void main(String[] args) {
        setDarkTheme();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private static void setDarkTheme() {
        try {
            // Global background colors
            UIManager.put("Panel.background", new Color(18, 25, 45));
            UIManager.put("Frame.background", new Color(18, 25, 45));
            UIManager.put("OptionPane.background", new Color(18, 25, 45));
            UIManager.put("OptionPane.messageForeground", Color.WHITE);
            
            // Increased default fonts for all components
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);
            Font boldFont = new Font("Segoe UI", Font.BOLD, 15);
            @SuppressWarnings("unused")
            Font smallBold = new Font("Segoe UI", Font.BOLD, 13);
            
            UIManager.put("Button.font", boldFont);
            UIManager.put("Button.background", new Color(70, 130, 200));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.select", new Color(40, 100, 160));
            
            UIManager.put("TextField.font", defaultFont);
            UIManager.put("TextField.background", new Color(30, 40, 60));
            UIManager.put("TextField.foreground", Color.WHITE);
            
            UIManager.put("TextArea.font", defaultFont);
            UIManager.put("TextArea.background", new Color(30, 40, 60));
            UIManager.put("TextArea.foreground", Color.WHITE);
            
            UIManager.put("PasswordField.font", defaultFont);
            UIManager.put("PasswordField.background", new Color(30, 40, 60));
            UIManager.put("PasswordField.foreground", Color.WHITE);
            
            UIManager.put("ComboBox.font", defaultFont);
            UIManager.put("ComboBox.background", new Color(40, 55, 85));
            UIManager.put("ComboBox.foreground", Color.WHITE);
            UIManager.put("ComboBox.selectionBackground", new Color(70, 130, 200));
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            
            UIManager.put("Label.font", defaultFont);
            UIManager.put("Label.foreground", Color.WHITE);
            
            UIManager.put("TabbedPane.font", boldFont);
            UIManager.put("TabbedPane.background", new Color(18, 25, 45));
            UIManager.put("TabbedPane.foreground", Color.WHITE);
            
            UIManager.put("TitledBorder.font", boldFont);
            UIManager.put("TitledBorder.titleColor", new Color(100, 180, 250));
            UIManager.put("TitledBorder.border", new LineBorder(new Color(60, 90, 130)));
            
            UIManager.put("OptionPane.font", defaultFont);
            UIManager.put("OptionPane.messageFont", defaultFont);
            UIManager.put("OptionPane.buttonFont", boldFont);
            
        } catch (Exception e) {}
    }
}

// ============ USER, APPLIANCE, etc. ============
class User {
    private String username, passwordHash, role;
    private double budget;
    private ArrayList<Appliance> appliances;
    private ArrayList<BillHistory> billHistory;
    public User(String username, String passwordHash, String role) {
        this.username = username; this.passwordHash = passwordHash; this.role = role;
        this.budget = 10000; this.appliances = new ArrayList<>(); this.billHistory = new ArrayList<>();
    }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public double getBudget() { return budget; }
    public void setBudget(double b) { budget = b; }
    public ArrayList<Appliance> getAppliances() { return appliances; }
    public ArrayList<BillHistory> getBillHistory() { return billHistory; }
}

class BillHistory {
    private LocalDateTime date;
    private double totalBill;
    private String month;
    public BillHistory(double totalBill) {
        this.date = LocalDateTime.now();
        this.totalBill = totalBill;
        this.month = date.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    }
    public String getMonth() { return month; }
    public double getTotalBill() { return totalBill; }
}

abstract class Appliance {
    protected String name, room, type;
    protected double wattage;
    protected int hoursPerDay;
    protected boolean isEssential;
    public Appliance(String name, double wattage, int hoursPerDay, String room, String type) {
        this.name = name; this.wattage = wattage; this.hoursPerDay = hoursPerDay;
        this.room = room; this.type = type; this.isEssential = false;
    }
    public abstract double calculateMonthlyCost(double rate);
    public String getName() { return name; }
    public double getWattage() { return wattage; }
    public int getHoursPerDay() { return hoursPerDay; }
    public String getRoom() { return room; }
    public String getType() { return type; }
    public boolean isEssential() { return isEssential; }
    public void setHoursPerDay(int h) { hoursPerDay = h; }
    public void setEssential(boolean e) { isEssential = e; }
}

class StandardAppliance extends Appliance {
    public StandardAppliance(String name, double wattage, int hoursPerDay, String room) {
        super(name, wattage, hoursPerDay, room, "Standard");
    }
    public double calculateMonthlyCost(double rate) {
        return ((wattage * hoursPerDay) / 1000.0) * 30 * rate;
    }
}

class HeavyAppliance extends Appliance {
    public HeavyAppliance(String name, double wattage, int hoursPerDay, String room) {
        super(name, wattage, hoursPerDay, room, "Heavy");
    }
    public double calculateMonthlyCost(double rate) {
        return ((wattage * hoursPerDay * 1.15) / 1000.0) * 30 * rate;
    }
}

class IntelligentAppliance extends Appliance {
    public IntelligentAppliance(String name, double wattage, int hoursPerDay, String room) {
        super(name, wattage, hoursPerDay, room, "Intelligent");
    }
    public double calculateMonthlyCost(double rate) {
        return ((wattage * hoursPerDay * 0.85) / 1000.0) * 30 * rate;
    }
}

// ============ AUTH SERVICE ============
class AuthService {
    private HashMap<String, User> users;
    private double rate;
    private final String DATA_DIR = "electricity_data";
    public AuthService() {
        users = new HashMap<>();
        rate = 35.0;
        new File(DATA_DIR).mkdirs();
        loadAllData();
        if (!users.containsKey("admin")) register("admin", "admin123", "admin");
    }
    private String hashPassword(String pwd) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pwd.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) { return pwd; }
    }
    private void loadAllData() {
        File f = new File(DATA_DIR + "/users.txt");
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length >= 3) {
                        User u = new User(p[0], p[1], p[2]);
                        if (p.length >= 4) u.setBudget(Double.parseDouble(p[3]));
                        users.put(p[0], u);
                    }
                }
            } catch (IOException e) {}
        }
        File rf = new File(DATA_DIR + "/rate.txt");
        if (rf.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(rf))) {
                rate = Double.parseDouble(br.readLine());
            } catch (IOException e) {}
        }
    }
    private void saveAllData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "/users.txt"))) {
            for (User u : users.values()) {
                bw.write(u.getUsername() + "," + u.getPasswordHash() + "," + u.getRole() + "," + u.getBudget());
                bw.newLine();
            }
        } catch (IOException e) {}
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIR + "/rate.txt"))) {
            bw.write(String.valueOf(rate));
        } catch (IOException e) {}
    }
    public boolean register(String user, String pwd, String role) {
        if (users.containsKey(user)) return false;
        users.put(user, new User(user, hashPassword(pwd), role));
        saveAllData();
        return true;
    }
    public User login(String user, String pwd) {
        User u = users.get(user);
        if (u != null && u.getPasswordHash().equals(hashPassword(pwd))) return u;
        return null;
    }
    public double getRate() { return rate; }
    public void setRate(double r) { rate = r; saveAllData(); }
    public void updateBudget(String user, double budget) {
        User u = users.get(user);
        if (u != null) { u.setBudget(budget); saveAllData(); }
    }
    public boolean isAdmin(User u) { return u != null && "admin".equals(u.getRole()); }
    public void saveUserData(User user) {
        String f = DATA_DIR + "/" + user.getUsername() + "_appliances.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            for (Appliance a : user.getAppliances()) {
                bw.write(a.getType() + "," + a.getName() + "," + a.getWattage() + "," + a.getHoursPerDay() + "," + a.getRoom() + "," + a.isEssential());
                bw.newLine();
            }
        } catch (IOException e) {}
        String hf = DATA_DIR + "/" + user.getUsername() + "_history.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(hf))) {
            for (BillHistory bh : user.getBillHistory()) {
                bw.write(bh.getMonth() + "," + bh.getTotalBill());
                bw.newLine();
            }
        } catch (IOException e) {}
    }
    public void loadUserData(User user) {
        String f = DATA_DIR + "/" + user.getUsername() + "_appliances.txt";
        File file = new File(f);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length >= 5) {
                        Appliance a;
                        if (p[0].equals("Heavy")) a = new HeavyAppliance(p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]), p[4]);
                        else if (p[0].equals("Intelligent")) a = new IntelligentAppliance(p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]), p[4]);
                        else a = new StandardAppliance(p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]), p[4]);
                        if (p.length > 5) a.setEssential(Boolean.parseBoolean(p[5]));
                        user.getAppliances().add(a);
                    }
                }
            } catch (IOException e) {}
        }
        String hf = DATA_DIR + "/" + user.getUsername() + "_history.txt";
        File hfile = new File(hf);
        if (hfile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(hfile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length >= 2) user.getBillHistory().add(new BillHistory(Double.parseDouble(p[1])));
                }
            } catch (IOException e) {}
        }
    }
    public void backupData(String backupDir) throws IOException {
        Path src = Paths.get(DATA_DIR);
        Path dest = Paths.get(backupDir);
        if (Files.exists(src)) {
            if (Files.exists(dest)) deleteDirectory(dest.toFile());
            Files.walk(src).forEach(source -> {
                try {
                    Path destination = dest.resolve(src.relativize(source));
                    if (Files.isDirectory(source)) { if (!Files.exists(destination)) Files.createDirectories(destination); }
                    else { Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING); }
                } catch (IOException e) { e.printStackTrace(); }
            });
        }
    }
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) for (File f : dir.listFiles()) deleteDirectory(f);
        dir.delete();
    }
    public void restoreData(String backupDir) throws IOException {
        Path backup = Paths.get(backupDir);
        if (Files.exists(backup)) {
            deleteDirectory(new File(DATA_DIR));
            Files.walk(backup).forEach(source -> {
                try {
                    Path destination = Paths.get(DATA_DIR).resolve(backup.relativize(source));
                    if (Files.isDirectory(source)) { if (!Files.exists(destination)) Files.createDirectories(destination); }
                    else { Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING); }
                } catch (IOException e) { e.printStackTrace(); }
            });
        }
    }
}

// ============ LOGIN FRAME ============
class LoginFrame extends JFrame {
    private AuthService auth;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    public LoginFrame() {
        auth = new AuthService();
        setTitle("⚡ Smart Electricity Bill Manager");
        setSize(580, 540);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(18,25,45));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30,45,75));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70,130,200),2),
            BorderFactory.createEmptyBorder(30,45,30,45)));
        JLabel titleLabel = new JLabel("⚡ SMART ELECTRICITY BILL MANAGER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(100,180,250));
        JLabel subLabel = new JLabel("Pakistan – Control Your Power, Save Your Money");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subLabel.setForeground(Color.LIGHT_GRAY);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(18,25,45));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20,20,20,20);
        JLabel userLabel = new JLabel("👤 USERNAME");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        userLabel.setForeground(Color.WHITE);
        gbc.gridx=0; gbc.gridy=0; formPanel.add(userLabel, gbc);
        usernameField = new JTextField(18); styleTextField(usernameField);
        gbc.gridx=1; formPanel.add(usernameField, gbc);
        JLabel passLabel = new JLabel("🔒 PASSWORD");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        passLabel.setForeground(Color.WHITE);
        gbc.gridx=0; gbc.gridy=1; formPanel.add(passLabel, gbc);
        passwordField = new JPasswordField(18); styleTextField(passwordField);
        gbc.gridx=1; formPanel.add(passwordField, gbc);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,25,0));
        btnPanel.setOpaque(false);
        JButton loginBtn = createBrightButton("LOGIN", new Color(46,204,113));
        JButton registerBtn = createBrightButton("REGISTER", new Color(52,152,219));
        btnPanel.add(loginBtn); btnPanel.add(registerBtn);
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; formPanel.add(btnPanel, gbc);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        gbc.gridy=3; formPanel.add(statusLabel, gbc);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> register());
    }
    private void styleTextField(JTextField tf) {
        tf.setBackground(new Color(30,45,70));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70,130,200)),
            BorderFactory.createEmptyBorder(10,14,10,14)));
    }
    private JButton createBrightButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12,30,12,30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }
    private void login() {
        User u = auth.login(usernameField.getText(), new String(passwordField.getPassword()));
        if (u != null) { auth.loadUserData(u); new DashboardFrame(u, auth).setVisible(true); dispose(); }
        else statusLabel.setText("❌ Invalid credentials");
    }
    private void register() {
        String u = usernameField.getText(), p = new String(passwordField.getPassword());
        if (u.isEmpty() || p.isEmpty()) statusLabel.setText("❌ Both fields required");
        else if (auth.register(u, p, "user")) statusLabel.setText("✅ Registered! Please login.");
        else statusLabel.setText("❌ Username exists");
    }
}

// ============ DASHBOARD FRAME ============
class DashboardFrame extends JFrame {
    private User currentUser;
    private AuthService auth;
    private JTabbedPane tabbedPane;
    private javax.swing.Timer realtimeTimer;
    private JLabel timerLabel, rateLabel, budgetLabel;
    private int seconds = 0;
    private DefaultListModel<String> applianceListModel;
    private JList<String> applianceJList;

    public DashboardFrame(User user, AuthService auth) {
        this.currentUser = user;
        this.auth = auth;
        setTitle("Dashboard - " + user.getUsername());
        setSize(1400, 920);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        startRealtimeTimer();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(18,25,45));

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(25,40,65));
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,2,0,new Color(70,130,200)),
            BorderFactory.createEmptyBorder(15,30,15,30)));
        JLabel welcomeLabel = new JLabel("🏠 Welcome, " + currentUser.getUsername() + (auth.isAdmin(currentUser) ? " 👑 ADMIN" : ""));
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(100,180,250));
        topBar.add(welcomeLabel, BorderLayout.WEST);
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,25,0));
        infoPanel.setOpaque(false);
        timerLabel = new JLabel("⏱️ 00:00:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timerLabel.setForeground(Color.WHITE);
        rateLabel = new JLabel("⚡ Rate: ₨" + String.format("%.2f", auth.getRate()) + "/kWh");
        rateLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rateLabel.setForeground(new Color(255,210,100));
        budgetLabel = new JLabel("💰 Budget: ₨" + String.format("%.2f", currentUser.getBudget()));
        budgetLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        budgetLabel.setForeground(new Color(100,255,150));
        infoPanel.add(timerLabel); infoPanel.add(rateLabel); infoPanel.add(budgetLabel);
        topBar.add(infoPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Tabbed Pane with custom painting (selected black, unselected light blue)
        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setFont(font);
                if (isSelected) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(new Color(100, 180, 250));
                }
                g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            }
        });
        tabbedPane.setBackground(new Color(18,25,45));
        // Tab font size set to 15 (bold) – slightly smaller than before
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabbedPane.addTab("📝 Manual Mode", createManualPanel());
        tabbedPane.addTab("🎯 Target Mode", createTargetPanel());
        tabbedPane.addTab("📊 Analytics", createAnalyticsPanel());
        tabbedPane.addTab("📈 Bill History", createHistoryPanel());
        tabbedPane.addTab("💡 Tips & Advice", createTipsPanel());
        if (auth.isAdmin(currentUser)) tabbedPane.addTab("👑 Admin", createAdminPanel());
        tabbedPane.addTab("⚙️ Settings", createSettingsPanel());
        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT,15,10));
        bottomBar.setBackground(new Color(25,40,65));
        JButton logoutBtn = createMenuButton("🚪 Logout", new Color(231,76,60));
        logoutBtn.addActionListener(e -> { auth.saveUserData(currentUser); new LoginFrame().setVisible(true); dispose(); });
        bottomBar.add(logoutBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JButton createMenuButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10,25,10,25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private JButton styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    // ========== MANUAL MODE ==========
    private JPanel createManualPanel() {
        JPanel panel = new JPanel(new BorderLayout(15,15));
        panel.setBackground(new Color(18,25,45));
        panel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(30,45,70));
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70,130,200)),
            "➕ ADD NEW APPLIANCE", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16), new Color(100,180,250)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12,18,12,18);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField nameField = new JTextField(15); styleField(nameField);
        JTextField wattField = new JTextField(15); styleField(wattField);
        JTextField hoursField = new JTextField(15); styleField(hoursField);
        JTextField roomField = new JTextField(15); styleField(roomField);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Standard","Heavy","Intelligent"});
        typeCombo.setBackground(new Color(40,60,90));
        typeCombo.setForeground(Color.WHITE);
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeCombo.setUI(new BasicComboBoxUI() {
            protected JButton createArrowButton() {
                JButton btn = super.createArrowButton();
                btn.setBackground(new Color(70,130,200));
                btn.setForeground(Color.WHITE);
                return btn;
            }
        });
        JCheckBox essentialCheck = new JCheckBox("Essential Appliance");
        essentialCheck.setBackground(new Color(30,45,70));
        essentialCheck.setForeground(Color.WHITE);
        essentialCheck.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton addBtn = new JButton("➕ ADD & CALCULATE");
        styleButton(addBtn, new Color(46,204,113));
        JTextArea resultArea = new JTextArea(8,40);
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(25,35,55));
        resultArea.setForeground(Color.WHITE);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        gbc.gridx=0; gbc.gridy=0; formPanel.add(new JLabel("Appliance Name:"), gbc);
        gbc.gridx=1; formPanel.add(nameField, gbc);
        gbc.gridx=0; gbc.gridy=1; formPanel.add(new JLabel("Wattage (W):"), gbc);
        gbc.gridx=1; formPanel.add(wattField, gbc);
        gbc.gridx=0; gbc.gridy=2; formPanel.add(new JLabel("Hours per Day:"), gbc);
        gbc.gridx=1; formPanel.add(hoursField, gbc);
        gbc.gridx=0; gbc.gridy=3; formPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx=1; formPanel.add(roomField, gbc);
        gbc.gridx=0; gbc.gridy=4; formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx=1; formPanel.add(typeCombo, gbc);
        gbc.gridx=0; gbc.gridy=5; gbc.gridwidth=2; formPanel.add(essentialCheck, gbc);
        gbc.gridy=6; formPanel.add(addBtn, gbc);

        // Right side JList for easy removal
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(30,45,70));
        listPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70,130,200)),
            "📋 YOUR APPLIANCES (click to select)", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16), new Color(100,180,250)));
        applianceListModel = new DefaultListModel<>();
        applianceJList = new JList<>(applianceListModel);
        applianceJList.setBackground(new Color(25,35,55));
        applianceJList.setForeground(Color.WHITE);
        applianceJList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        applianceJList.setSelectionBackground(new Color(70,130,200));
        JButton refreshListBtn = new JButton("🔄 REFRESH LIST");
        styleButton(refreshListBtn, new Color(22,160,133));
        JButton removeSelectedBtn = new JButton("🗑️ REMOVE SELECTED");
        styleButton(removeSelectedBtn, new Color(231,76,60));
        JPanel listBtnPanel = new JPanel(new FlowLayout());
        listBtnPanel.setOpaque(false);
        listBtnPanel.add(refreshListBtn);
        listBtnPanel.add(removeSelectedBtn);
        listPanel.add(new JScrollPane(applianceJList), BorderLayout.CENTER);
        listPanel.add(listBtnPanel, BorderLayout.SOUTH);

        refreshListBtn.addActionListener(e -> refreshApplianceList());
        removeSelectedBtn.addActionListener(e -> {
            int idx = applianceJList.getSelectedIndex();
            if (idx >= 0) {
                String selected = applianceListModel.get(idx);
                String name = selected.split(" \\(")[0];
                currentUser.getAppliances().removeIf(a -> a.getName().equals(name));
                auth.saveUserData(currentUser);
                refreshApplianceList();
                refreshAllTabs();
                JOptionPane.showMessageDialog(panel, "✅ Removed: " + name);
            } else JOptionPane.showMessageDialog(panel, "Select an appliance first");
        });

        addBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                double watt = Double.parseDouble(wattField.getText().trim());
                int hours = Integer.parseInt(hoursField.getText().trim());
                String room = roomField.getText().trim();
                String type = (String) typeCombo.getSelectedItem();
                Appliance a;
                if (type.equals("Heavy")) a = new HeavyAppliance(name, watt, hours, room);
                else if (type.equals("Intelligent")) a = new IntelligentAppliance(name, watt, hours, room);
                else a = new StandardAppliance(name, watt, hours, room);
                a.setEssential(essentialCheck.isSelected());
                currentUser.getAppliances().add(a);
                auth.saveUserData(currentUser);
                double cost = a.calculateMonthlyCost(auth.getRate());
                resultArea.setText("✅ Added! " + name + " (" + type + ") → ₨" + String.format("%.2f", cost) + "/month");
                nameField.setText(""); wattField.setText(""); hoursField.setText(""); roomField.setText("");
                essentialCheck.setSelected(false);
                refreshApplianceList();
                refreshAllTabs();
            } catch (Exception ex) { resultArea.setText("❌ Invalid input"); }
        });

        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(formPanel, BorderLayout.NORTH);
        leftWrapper.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        panel.add(leftWrapper, BorderLayout.WEST);
        panel.add(listPanel, BorderLayout.CENTER);
        refreshApplianceList();
        return panel;
    }

    private void refreshApplianceList() {
        applianceListModel.clear();
        for (Appliance a : currentUser.getAppliances()) {
            double cost = a.calculateMonthlyCost(auth.getRate());
            applianceListModel.addElement(String.format("%s (%s) - %s - %.0fW x %dh = ₨%.2f/m",
                a.getName(), a.getRoom(), a.getType(), a.getWattage(), a.getHoursPerDay(), cost));
        }
    }

    // ========== TARGET MODE ==========
    private JPanel createTargetPanel() {
        JPanel panel = new JPanel(new BorderLayout(15,15));
        panel.setBackground(new Color(18,25,45));
        panel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBackground(new Color(30,45,70));
        inputPanel.setBorder(BorderFactory.createTitledBorder("🎯 SET YOUR MONTHLY BUDGET"));
        inputPanel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel budgetLabel = new JLabel("Budget (₨):");
        budgetLabel.setForeground(Color.WHITE);
        budgetLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField budgetField = new JTextField(String.valueOf(currentUser.getBudget()),12);
        styleField(budgetField);
        JButton setBudgetBtn = new JButton("💰 SET BUDGET");
        JButton optimizeBtn = new JButton("⚡ OPTIMIZE USAGE");
        styleButton(setBudgetBtn, new Color(52,152,219));
        styleButton(optimizeBtn, new Color(241,196,15));
        inputPanel.add(budgetLabel); inputPanel.add(budgetField);
        inputPanel.add(setBudgetBtn); inputPanel.add(optimizeBtn);
        JTextArea resultArea = new JTextArea(15,60);
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(25,35,55));
        resultArea.setForeground(Color.WHITE);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        setBudgetBtn.addActionListener(e -> {
            try { double b = Double.parseDouble(budgetField.getText());
                auth.updateBudget(currentUser.getUsername(), b);
                currentUser.setBudget(b);
                refreshAllTabs();
                JOptionPane.showMessageDialog(panel, "✅ Budget updated");
            } catch(Exception ex) { JOptionPane.showMessageDialog(panel, "Invalid number"); }
        });
        optimizeBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            double total = 0;
            for (Appliance a : currentUser.getAppliances()) total += a.calculateMonthlyCost(auth.getRate());
            sb.append("===== OPTIMIZATION REPORT =====\n\n");
            sb.append(String.format("Current Bill: ₨%.2f\nBudget: ₨%.2f\n", total, currentUser.getBudget()));
            if (total > currentUser.getBudget()) {
                sb.append(String.format("Over budget: ₨%.2f\n\n", total - currentUser.getBudget()));
                sb.append("RECOMMENDATIONS:\n");
                sb.append("• Reduce AC/Heater usage by 2–4 hours daily\n");
                sb.append("• Replace bulbs with LEDs (save up to 75%)\n");
                sb.append("• Unplug idle devices (standby power 5-10%)\n");
                sb.append("• Use fan instead of AC when possible\n");
                sb.append("• Shift heavy tasks to off-peak hours (after 10 PM)\n");
            } else sb.append("✅ Within budget! Remaining: ₨" + (currentUser.getBudget()-total) + "\nGreat job!");
            resultArea.setText(sb.toString());
        });
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    // ========== ANALYTICS ==========
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBackground(new Color(18,25,45));
        panel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        JTextArea analyticsArea = new JTextArea(20,60);
        analyticsArea.setEditable(false);
        analyticsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        analyticsArea.setBackground(new Color(25,35,55));
        analyticsArea.setForeground(Color.WHITE);
        JButton refreshBtn = new JButton("🔄 REFRESH ANALYTICS");
        styleButton(refreshBtn, new Color(52,152,219));
        refreshBtn.addActionListener(e -> updateAnalytics(analyticsArea));
        panel.add(refreshBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(analyticsArea), BorderLayout.CENTER);
        updateAnalytics(analyticsArea);
        return panel;
    }
    private void updateAnalytics(JTextArea area) {
        StringBuilder sb = new StringBuilder();
        sb.append("========== DETAILED MONTHLY ANALYSIS ==========\n\n");
        double total = 0;
        HashMap<String,Double> roomCost = new HashMap<>();
        HashMap<String,Double> typeCost = new HashMap<>();
        for (Appliance a : currentUser.getAppliances()) {
            double cost = a.calculateMonthlyCost(auth.getRate());
            total += cost;
            roomCost.put(a.getRoom(), roomCost.getOrDefault(a.getRoom(),0.0)+cost);
            typeCost.put(a.getType(), typeCost.getOrDefault(a.getType(),0.0)+cost);
        }
        sb.append(String.format("💰 TOTAL BILL: ₨%.2f\n", total));
        sb.append(String.format("📋 BUDGET: ₨%.2f\n", currentUser.getBudget()));
        sb.append(String.format("📊 STATUS: %s\n\n", total>currentUser.getBudget()?"⚠️ OVER BUDGET":"✅ WITHIN BUDGET"));
        sb.append("--- 🏠 COST PER ROOM ---\n");
        for (Map.Entry<String,Double> e : roomCost.entrySet()) sb.append(String.format("   %s: ₨%.2f\n", e.getKey(), e.getValue()));
        sb.append("\n--- 📱 COST BY APPLIANCE TYPE ---\n");
        for (Map.Entry<String,Double> e : typeCost.entrySet()) sb.append(String.format("   %s: ₨%.2f\n", e.getKey(), e.getValue()));
        sb.append("\n--- 🔌 APPLIANCE BREAKDOWN ---\n");
        for (Appliance a : currentUser.getAppliances()) {
            double c = a.calculateMonthlyCost(auth.getRate());
            sb.append(String.format("   %s (%s) [%s]: %.0fW x %dh/day → ₨%.2f/month\n", a.getName(), a.getRoom(), a.getType(), a.getWattage(), a.getHoursPerDay(), c));
        }
        area.setText(sb.toString());
    }

    // ========== BILL HISTORY ==========
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBackground(new Color(18,25,45));
        panel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        JTextArea historyArea = new JTextArea(15,60);
        historyArea.setEditable(false);
        historyArea.setBackground(new Color(25,35,55));
        historyArea.setForeground(Color.WHITE);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JButton saveBtn = new JButton("💾 SAVE CURRENT MONTH");
        styleButton(saveBtn, new Color(46,204,113));
        JPanel btnPanel = new JPanel(); btnPanel.add(saveBtn);
        saveBtn.addActionListener(e -> {
            double total = 0;
            for (Appliance a : currentUser.getAppliances()) total += a.calculateMonthlyCost(auth.getRate());
            currentUser.getBillHistory().add(new BillHistory(total));
            auth.saveUserData(currentUser);
            updateHistory(historyArea);
            JOptionPane.showMessageDialog(panel, "✅ History saved");
        });
        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        updateHistory(historyArea);
        return panel;
    }
    private void updateHistory(JTextArea area) {
        StringBuilder sb = new StringBuilder();
        sb.append("========== MONTHLY BILL HISTORY ==========\n\n");
        if (currentUser.getBillHistory().isEmpty()) sb.append("No records yet. Click 'Save Current Month'.");
        else for (BillHistory bh : currentUser.getBillHistory()) sb.append(String.format("📅 %s: ₨%.2f\n", bh.getMonth(), bh.getTotalBill()));
        area.setText(sb.toString());
    }

    // ========== TIPS ==========
    private JPanel createTipsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(18,25,45));
        panel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        JTextArea tips = new JTextArea();
        tips.setEditable(false);
        tips.setBackground(new Color(25,35,55));
        tips.setForeground(Color.WHITE);
        tips.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tips.setText(
            "💡 ENERGY SAVING TIPS (PAKISTAN)\n\n" +
            "🔹 LIGHTING:\n   • Replace all bulbs with LEDs – saves up to 75% electricity\n   • Turn off lights when leaving a room\n\n" +
            "🔹 COOLING / HEATING:\n   • Set AC to 24–26°C – every degree lower increases cost ~6%\n   • Clean filters monthly for efficiency\n   • Use ceiling fans – they consume only 75W vs 1500W for AC\n\n" +
            "🔹 REFRIGERATOR:\n   • Keep at 3–5°C, freezer at -18°C\n   • Don't put hot food directly inside\n   • Check door seals regularly\n\n" +
            "🔹 WASHING & KITCHEN:\n   • Run full loads only\n   • Use cold water wash cycle\n   • Microwave uses 50% less than oven\n\n" +
            "🔹 STANDBY POWER:\n   • Unplug phone chargers, TV, computer when not in use\n   • Use power strips to cut off multiple devices at once\n\n" +
            "🔹 BUDGET OPTIMIZATION:\n   • Track daily usage with this app\n   • Shift heavy loads (iron, washing machine) to off-peak hours (after 10 PM)\n   • Invest in inverter appliances – they save 15–20% electricity\n\n" +
            "🔹 PAKISTAN SPECIFIC:\n   • Current average rate: ₨35–45/kWh\n   • Peak hours: 6 PM – 10 PM, avoid using heavy appliances then\n   • Consider joining government's energy conservation programmes\n\n" +
            "💰 Share these tips with family and start saving today!");
        panel.add(new JScrollPane(tips), BorderLayout.CENTER);
        return panel;
    }

    // ========== ADMIN PANEL ==========
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(18,25,45));
        panel.setBorder(BorderFactory.createEmptyBorder(35,35,35,35));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20,20,20,20);
        JLabel currentRate = new JLabel("Current Rate: ₨" + String.format("%.2f", auth.getRate()) + "/kWh");
        currentRate.setFont(new Font("Segoe UI", Font.BOLD, 18));
        currentRate.setForeground(new Color(100,180,250));
        JLabel newRateLabel = new JLabel("Set New Rate (₨/kWh):");
        newRateLabel.setForeground(Color.WHITE);
        newRateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        JTextField rateField = new JTextField(10);
        styleField(rateField);
        JButton updateBtn = new JButton("UPDATE RATE");
        styleButton(updateBtn, new Color(46,204,113));
        updateBtn.addActionListener(e -> {
            try { double nr = Double.parseDouble(rateField.getText());
                auth.setRate(nr);
                currentRate.setText("Current Rate: ₨" + String.format("%.2f", nr) + "/kWh");
                rateLabel.setText("⚡ Rate: ₨" + String.format("%.2f", nr) + "/kWh");
                JOptionPane.showMessageDialog(panel, "Rate updated");
                refreshAllTabs();
            } catch(Exception ex) { JOptionPane.showMessageDialog(panel, "Invalid number"); }
        });
        gbc.gridx=0; gbc.gridy=0; panel.add(currentRate, gbc);
        gbc.gridy=1; panel.add(newRateLabel, gbc);
        gbc.gridx=1; panel.add(rateField, gbc);
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; panel.add(updateBtn, gbc);
        return panel;
    }

    // ========== SETTINGS PANEL ==========
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(18,25,45));
        panel.setBorder(BorderFactory.createEmptyBorder(35,35,35,35));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20,20,20,20);

        JLabel rateTitle = new JLabel("⚡ UPDATE ELECTRICITY RATE");
        rateTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        rateTitle.setForeground(new Color(100,180,250));
        JPanel ratePanel = new JPanel(new FlowLayout());
        ratePanel.setOpaque(false);
        JLabel currentRateLabel = new JLabel("Current: ₨" + String.format("%.2f", auth.getRate()) + "/kWh");
        currentRateLabel.setForeground(Color.WHITE);
        currentRateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField newRateField = new JTextField(8);
        styleField(newRateField);
        JButton updateRateBtn = new JButton("UPDATE");
        updateRateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        updateRateBtn.addActionListener(e -> {
            try {
                double newRate = Double.parseDouble(newRateField.getText());
                auth.setRate(newRate);
                currentRateLabel.setText("Current: ₨" + String.format("%.2f", newRate) + "/kWh");
                rateLabel.setText("⚡ Rate: ₨" + String.format("%.2f", newRate) + "/kWh");
                JOptionPane.showMessageDialog(panel, "✅ Rate updated to ₨" + newRate);
                refreshAllTabs();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Invalid number");
            }
        });
        ratePanel.add(currentRateLabel);
        ratePanel.add(new JLabel("New Rate:"));
        ratePanel.add(newRateField);
        ratePanel.add(updateRateBtn);

        JButton backupBtn = new JButton("💾 BACKUP DATA");
        styleButton(backupBtn, new Color(52,152,219));
        JButton restoreBtn = new JButton("🔄 RESTORE DATA");
        styleButton(restoreBtn, new Color(155,89,182));
        JButton pdfBtn = new JButton("📄 EXPORT FULL REPORT (PDF)");
        styleButton(pdfBtn, new Color(230,126,34));
        JButton clearBtn = new JButton("🗑️ CLEAR ALL APPLIANCES");
        styleButton(clearBtn, new Color(231,76,60));

        backupBtn.addActionListener(e -> performBackup());
        restoreBtn.addActionListener(e -> performRestore());
        pdfBtn.addActionListener(e -> generateFullPDFWithChooser());
        clearBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(panel, "Delete ALL appliances?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                currentUser.getAppliances().clear();
                auth.saveUserData(currentUser);
                refreshAllTabs();
                JOptionPane.showMessageDialog(panel, "Cleared all appliances");
            }
        });

        gbc.gridx=0; gbc.gridy=0; panel.add(rateTitle, gbc);
        gbc.gridy=1; panel.add(ratePanel, gbc);
        gbc.gridy=2; panel.add(backupBtn, gbc);
        gbc.gridy=3; panel.add(restoreBtn, gbc);
        gbc.gridy=4; panel.add(pdfBtn, gbc);
        gbc.gridy=5; panel.add(clearBtn, gbc);
        return panel;
    }

    private void performBackup() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Backup Destination Folder");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String backupFolder = chooser.getSelectedFile().getAbsolutePath() + "/electricity_backup_" + System.currentTimeMillis();
                auth.backupData(backupFolder);
                JOptionPane.showMessageDialog(this, "✅ Backup created at:\n" + backupFolder);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage()); }
        }
    }

    private void performRestore() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Backup Folder to Restore");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int confirm = JOptionPane.showConfirmDialog(this, "Restoring will replace current data. Continue?", "Restore", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    auth.restoreData(chooser.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "✅ Restore complete. Please restart application.");
                    System.exit(0);
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Restore failed: " + ex.getMessage()); }
            }
        }
    }

    private void generateFullPDFWithChooser() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setSelectedFile(new File("electricity_report_" + currentUser.getUsername() + "_" + System.currentTimeMillis() + ".pdf"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = chooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".pdf")) filePath += ".pdf";
            generateFullPDF(filePath);
        }
    }

    private void generateFullPDF(String filePath) {
        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, new FileOutputStream(filePath));
            doc.open();

            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("ELECTRICITY BILL MANAGEMENT REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph(" "));

            com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            doc.add(new Paragraph("User: " + currentUser.getUsername(), normalFont));
            doc.add(new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
            doc.add(new Paragraph("Electricity Rate: ₨" + String.format("%.2f", auth.getRate()) + "/kWh", normalFont));
            doc.add(new Paragraph("Monthly Budget: ₨" + String.format("%.2f", currentUser.getBudget()), normalFont));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            table.addCell(new PdfPCell(new Paragraph("Name", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("Type", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("Wattage", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("Hours/Day", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("Room", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("Essential", headerFont)));
            table.addCell(new PdfPCell(new Paragraph("Monthly Cost (₨)", headerFont)));

            double total = 0;
            for (Appliance a : currentUser.getAppliances()) {
                double cost = a.calculateMonthlyCost(auth.getRate());
                total += cost;
                table.addCell(new PdfPCell(new Paragraph(a.getName(), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(a.getType(), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf((int)a.getWattage()), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(a.getHoursPerDay()), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(a.getRoom(), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(a.isEssential() ? "Yes" : "No", normalFont)));
                table.addCell(new PdfPCell(new Paragraph(String.format("%.2f", cost), normalFont)));
            }
            doc.add(table);
            doc.add(new Paragraph(" "));
            com.itextpdf.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph totalPara = new Paragraph("TOTAL MONTHLY BILL: ₨" + String.format("%.2f", total), boldFont);
            totalPara.setAlignment(Element.ALIGN_RIGHT);
            doc.add(totalPara);

            if (!currentUser.getBillHistory().isEmpty()) {
                doc.add(new Paragraph(" ", normalFont));
                doc.add(new Paragraph("BILL HISTORY", headerFont));
                PdfPTable histTable = new PdfPTable(2);
                histTable.setWidthPercentage(50);
                histTable.addCell(new PdfPCell(new Paragraph("Month", headerFont)));
                histTable.addCell(new PdfPCell(new Paragraph("Amount (₨)", headerFont)));
                for (BillHistory bh : currentUser.getBillHistory()) {
                    histTable.addCell(new PdfPCell(new Paragraph(bh.getMonth(), normalFont)));
                    histTable.addCell(new PdfPCell(new Paragraph(String.format("%.2f", bh.getTotalBill()), normalFont)));
                }
                doc.add(histTable);
            }

            doc.close();
            JOptionPane.showMessageDialog(this, "✅ PDF Report saved:\n" + filePath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "PDF error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== UTILITIES ==========
    private void styleField(JTextField tf) {
        tf.setBackground(new Color(30,45,70));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70,130,200)),
            BorderFactory.createEmptyBorder(8,12,8,12)));
    }

    private void refreshAllTabs() {
        int idx = tabbedPane.indexOfTab("📊 Analytics");
        if (idx != -1) tabbedPane.setComponentAt(idx, createAnalyticsPanel());
        idx = tabbedPane.indexOfTab("📈 Bill History");
        if (idx != -1) tabbedPane.setComponentAt(idx, createHistoryPanel());
        idx = tabbedPane.indexOfTab("📝 Manual Mode");
        if (idx != -1) tabbedPane.setComponentAt(idx, createManualPanel());
        budgetLabel.setText("💰 Budget: ₨" + String.format("%.2f", currentUser.getBudget()));
    }

    private void startRealtimeTimer() {
        realtimeTimer = new javax.swing.Timer(1000, e -> {
            seconds++; int h = seconds/3600, m = (seconds%3600)/60, s = seconds%60;
            timerLabel.setText(String.format("⏱️ %02d:%02d:%02d", h, m, s));
        });
        realtimeTimer.start();
    }
}