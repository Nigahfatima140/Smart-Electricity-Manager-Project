import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.ArrayList;

class AuthService {
    private ArrayList<User> users = new ArrayList<>();
    private double rate = 35.0;
    @SuppressWarnings("unused")
    private final String DATA_DIR = "electricity_data";

    public AuthService() {
        new File("electricity_data").mkdirs();
        loadAllData();
        if (findUser("admin") == null) {
            register("admin", "admin123", "admin");
        }
    }

    // ── helper: find user by username ──────────────
    private User findUser(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null; // not found
    }

    // ── helper: check if username exists (replaces HashMap.containsKey()) ───
    private boolean userExists(String username) {
        return findUser(username) != null;
    }

    // ── password hashing ─────────────────────────────────────────────────────
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }

    // ── load all data from files ─────────────────────────────────────────────
    private void loadAllData() {
        File usersFile = new File("electricity_data/users.txt");
        if (usersFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        User u = new User(parts[0], parts[1], parts[2]);
                        if (parts.length >= 4) {
                            u.setBudget(Double.parseDouble(parts[3]));
                        }
                        users.add(u); // ArrayList.add() 
                    }
                }
            } catch (IOException e) {}
        }

        File rateFile = new File("electricity_data/rate.txt");
        if (rateFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(rateFile))) {
                rate = Double.parseDouble(br.readLine());
            } catch (IOException e) {}
        }
    }

    // ── save all data to files ───────────────────────────────────────────────
    private void saveAllData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("electricity_data/users.txt"))) {
            for (User u : users) { // loop ArrayList 
                bw.write(u.getUsername() + "," + u.getPasswordHash() + ","
                        + u.getRole() + "," + u.getBudget());
                bw.newLine();
            }
        } catch (IOException e) {}

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("electricity_data/rate.txt"))) {
            bw.write(String.valueOf(rate));
        } catch (IOException e) {}
    }

    // ── register new user ────────────────────────────────────────────────────
    public boolean register(String username, String password, String role) {
        if (userExists(username)) { // uses findUser() loop instead of containsKey()
            return false;
        }
        users.add(new User(username, hashPassword(password), role));
        saveAllData();
        return true;
    }

    // ── login ────────────────────────────────────────────────────────────────
    public User login(String username, String password) {
        User u = findUser(username); // uses findUser() loop instead of HashMap.get()
        if (u != null && u.getPasswordHash().equals(hashPassword(password))) {
            return u;
        }
        return null;
    }

    // ── rate management ──────────────────────────────────────────────────────
    public double getRate() { return rate; }

    public void setRate(double newRate) {
        rate = newRate;
        saveAllData();
    }

    // ── budget update ────────────────────────────────────────────────────────
    public void updateBudget(String username, double budget) {
        User u = findUser(username); 
        if (u != null) {
            u.setBudget(budget);
            saveAllData();
        }
    }

    // ── admin check ──────────────────────────────────────────────────────────
    public boolean isAdmin(User u) {
        return u != null && "admin".equals(u.getRole());
    }

    // ── save user appliances & history ───────────────────────────────────────
    public void saveUserData(User user) {
        String appPath = "electricity_data/" + user.getUsername() + "_appliances.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(appPath))) {
            for (Appliance a : user.getAppliances()) {
                bw.write(a.getType() + "," + a.getName() + "," + a.getWattage()
                        + "," + a.getHoursPerDay() + "," + a.getRoom()
                        + "," + a.isEssential());
                bw.newLine();
            }
        } catch (IOException e) {}

        String histPath = "electricity_data/" + user.getUsername() + "_history.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(histPath))) {
            for (BillHistory h : user.getBillHistory()) {
                bw.write(h.getMonth() + "," + h.getTotalBill());
                bw.newLine();
            }
        } catch (IOException e) {}
    }

    // ── load user appliances & history ───────────────────────────────────────
    public void loadUserData(User user) {
        File appFile = new File("electricity_data/" + user.getUsername() + "_appliances.txt");
        if (appFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(appFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length >= 5) {
                        Appliance a;
                        if      (p[0].equals("Heavy"))       a = new HeavyAppliance(p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]), p[4]);
                        else if (p[0].equals("Intelligent")) a = new IntelligentAppliance(p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]), p[4]);
                        else                                 a = new StandardAppliance(p[1], Double.parseDouble(p[2]), Integer.parseInt(p[3]), p[4]);
                        if (p.length > 5) a.setEssential(Boolean.parseBoolean(p[5]));
                        user.getAppliances().add(a);
                    }
                }
            } catch (IOException e) {}
        }

        File histFile = new File("electricity_data/" + user.getUsername() + "_history.txt");
        if (histFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(histFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length >= 2) {
                        user.getBillHistory().add(new BillHistory(Double.parseDouble(p[1])));
                    }
                }
            } catch (IOException e) {}
        }
    }

    // ── backup & restore ─────────────────────────────────────────────────────
    public void backupData(String destPath) throws IOException {
        Path src  = Paths.get("electricity_data");
        Path dest = Paths.get(destPath);//destination choosen by user
        if (Files.exists(src)) {
            if (Files.exists(dest)) deleteDirectory(dest.toFile());
            Files.walk(src).forEach(s -> { //visits every file inside source
                try {
                    Path d = dest.resolve(src.relativize(s));
                    if (Files.isDirectory(s)) { if (!Files.exists(d)) Files.createDirectories(d); }
                    else Files.copy(s, d, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) { e.printStackTrace(); }
            });
        }
    }

    public void restoreData(String srcPath) throws IOException {
        Path src = Paths.get(srcPath);
        if (Files.exists(src)) {
            deleteDirectory(new File("electricity_data"));
            Files.walk(src).forEach(s -> {
                try {
                    Path d = Paths.get("electricity_data").resolve(src.relativize(s));
                    if (Files.isDirectory(s)) { if (!Files.exists(d)) Files.createDirectories(d); }
                    else Files.copy(s, d, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) { e.printStackTrace(); }
            });
        }
    }

    private void deleteDirectory(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) deleteDirectory(child);
        }
        file.delete();
    }
}