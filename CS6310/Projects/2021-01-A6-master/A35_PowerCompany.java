import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class A35_PowerCompany extends JFrame {
    // TODO: 
    // We can define this in the config file instead.
    public enum CustomerType { LOW_CONSUMER, HIGH_CONSUMER }

    // Simulates applicance usage. 
    // Predefined events listed in config file.
    public static class ApplianceEvent {
        private final String name;
        private final double kwUsage;

        public ApplianceEvent(String name, double kwUsage) {
            this.name = name;
            this.kwUsage = kwUsage;
        }

        public String getName() { return name; }
        public double getKwUsage() { return kwUsage; }
    }

    /*
     * We will have two types of custmors:
     *  LOW_CONSUMER
     *  HIGH_CONSUMER
     */
    public static class Customer {
        private final int id;
        private final String name;
        private final CustomerType type;
        private double totalKwConsumed;

        public Customer(int id, String name, CustomerType type) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.totalKwConsumed = 0.0;
        }

        public int getId() { 
            return id; 
        }
        public String getName() { 
            return name; 
        }
        public CustomerType getType() { 
            return type; 
        }
        public double getTotalKwConsumed() { 
            return totalKwConsumed; 
        }
        public void consume(double kw) { 
            this.totalKwConsumed += kw; 
        }
    }

    // This one is pretty straighforward. 
    // We need to limit the # of transformers per substation to MAX_TRANSFORMERS (see config).
    public static class Transformer {
        private final int id;
        public Transformer(int id) { 
            this.id = id; 
        }
        public int getId() { 
            return id; 
        }
    }

    // Pretty straightforward
    public static class Substation {
        private final int id;
        private final List<Transformer> transformers = new ArrayList<>();
        public static final int MAX_TRANSFORMERS = 20;

        public Substation(int id) {
            this.id = id;
            for (int i = 1; i <= MAX_TRANSFORMERS; i++) {
                transformers.add(new Transformer(i));
            }
        }

        public int getId() { return id; }
        public List<Transformer> getTransformers() { return transformers; }
    }

    // Pretty straightforward
    public static class PowerStation {
        private final String name;
        private final List<Substation> substations = new ArrayList<>();

        public PowerStation(String name) {
            this.name = name;
            substations.add(new Substation(1));
            substations.add(new Substation(2));
        }

        public String getName() { return name; }
        public List<Substation> getSubstations() { return substations; }
    }

    public static class Employee {
        private final int id;
        private final String name;

        public Employee(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getName() { return name; }
    }

    // List of predefined issues listed in the config file.
    public static class MaintenanceIssue {
        private final int cycle;
        private final String issueType;
        private final int fixTimeMinutes;
        private final double fixCost;
        private final Employee assignedWorker;

        public MaintenanceIssue(int cycle, String issueType, 
                                int fixTimeMinutes, double fixCost, 
                                Employee assignedWorker) {
            this.cycle = cycle;
            this.issueType = issueType;
            this.fixTimeMinutes = fixTimeMinutes;
            this.fixCost = fixCost;
            this.assignedWorker = assignedWorker;
        }

        public double getFixCost() { return fixCost; }

        @Override
        public String toString() {
            return String.format("[Cycle %d] %s | Assigned to: %s | Time to fix: %d mins | Cost: $%.2f",
                    cycle, issueType, assignedWorker.getName(), fixTimeMinutes, fixCost);
        }
    }

    public static class PowerCompany {
        private final String name;
        private final PowerStation powerStation;
        private final List<Customer> customers = new ArrayList<>();
        private final List<Employee> employees = new ArrayList<>();
        private final List<MaintenanceIssue> issuesLog = new ArrayList<>();

        public PowerCompany(String name) {
            this.name = name;
            this.powerStation = new PowerStation("Station # 1");
            // Create customers 
            initCustomers();
            // Create employees 
            initEmployees();
        }

        private void initEmployees() {
            employees.add(new Employee(101, "Alice Smith"));
            employees.add(new Employee(102, "Bob Jones"));
            employees.add(new Employee(103, "Charlie Davis"));
        }

        private void initCustomers() {
            // Need to read list from config file
            // 5 Low Consumers
            for (int i = 1; i <= 5; i++) {
                customers.add(new Customer(i, "Low Consumer #" + i, CustomerType.LOW_CONSUMER));
            }
            // 5 High Consumers
            for (int i = 6; i <= 10; i++) {
                customers.add(new Customer(i, "High Consumer #" + i, CustomerType.HIGH_CONSUMER));
            }
        }

        public List<Customer> getCustomers() { 
            return customers; 
        }
        public List<Employee> getEmployees() { 
            return employees; 
        }
        public List<MaintenanceIssue> getIssuesLog() { 
            return issuesLog; 
        }
        public PowerStation getPowerStation() { 
            return powerStation; 
        }

        public void logIssue(MaintenanceIssue issue) {
            issuesLog.add(issue);
        }

        public double getTotalRepairCost() {
            return issuesLog.stream().mapToDouble(MaintenanceIssue::getFixCost).sum();
        }
    }

    private final PowerCompany powerCompany;
    private final List<ApplianceEvent> applianceEvents = new ArrayList<>();
    private final Random random = new Random();
    private int currentCycle = 0;
    private Timer simTimer;

    // UI elements
    private JTextArea logTextArea;
    private JTextArea consumptionTextArea;
    private JTextArea issuesTextArea;
    private JLabel cycleLabel;
    private JLabel totalCostLabel;
    private JButton startButton;
    private JButton stopButton;

    public A35_PowerCompany() {
        super("A35 Power Company");
        this.powerCompany = new PowerCompany("GT PW Inc.");
        initApplianceEvents();
        setupUI();
    }

    private void initApplianceEvents() {
        applianceEvents.add(new ApplianceEvent("Turn on LED Light", 0.015));
        applianceEvents.add(new ApplianceEvent("Turn on TV", 0.15));
        applianceEvents.add(new ApplianceEvent("Run Washing Machine", 0.50));
        applianceEvents.add(new ApplianceEvent("Run Microwave", 1.20));
        applianceEvents.add(new ApplianceEvent("Turn AC On", 3.50));
        applianceEvents.add(new ApplianceEvent("EV Charging", 7.20));
    }

    private void runMonthlyCycle() {
        currentCycle++;
        logTextArea.append("=== STARTING CYCLE " + currentCycle + " ===\n");

        Substation sub = powerCompany.getPowerStation().getSubstations().get(0);
        Transformer trans = sub.getTransformers().get(0);
        logTextArea.append(String.format("Power Grid Transmission: [%s] -> [Substation #%d] -> [Transformer #%d] -> [Grid Grid]\n",
                powerCompany.getPowerStation().getName(), sub.getId(), trans.getId()));

        /* 
         * Random customer consumption
         */
        int activeCustomersCount = random.nextInt(5) + 1; // 1 to 5 customers act this cycle
        for (int i = 0; i < activeCustomersCount; i++) {
            Customer c = powerCompany.getCustomers().get(random.nextInt(powerCompany.getCustomers().size()));
            ApplianceEvent event = applianceEvents.get(random.nextInt(applianceEvents.size()));

            // High consumers use multiplier
            double multiplier = (c.getType() == CustomerType.HIGH_CONSUMER) ? 2.5 : 1.0;
            double kwUsed = event.getKwUsage() * multiplier;
            c.consume(kwUsed);

            logTextArea.append(String.format("  - %s (%s) used '%s' consuming %.3f kW\n",
                    c.getName(), c.getType(), event.getName(), kwUsed));
        }

        /*
         * Randomize issues per cycle
         * Not all cycles will have issues
         */
        if (random.nextBoolean()) {
            String[] possibleIssues = { 
                "Transformer Breakdown", 
                "Cable Rupture", 
                "Substation Surge", 
                "Insulation Failure", 
                "Tree Branch Short" 
            };

            String issueName = possibleIssues[random.nextInt(possibleIssues.length)];
            int fixTime = random.nextInt(120) + 15; // 15 to 135 mins
            double fixCost = 150.0 + (random.nextDouble() * 1850.0); // $150 to $2000
            Employee assigned = powerCompany.getEmployees().get(random.nextInt(powerCompany.getEmployees().size()));

            MaintenanceIssue issue = new MaintenanceIssue(currentCycle, issueName, fixTime, fixCost, assigned);
            powerCompany.logIssue(issue);
            logTextArea.append("  Power System Issue: " + issue + "\n");
        } else {
            logTextArea.append("  Grid Status: No issues reported.\n");
        }

        logTextArea.append("\n");
        updateSummaryViews();
    }

    private void updateSummaryViews() {
        cycleLabel.setText("Cycle: " + currentCycle);

        // Update Customers Table View
        StringBuilder sbCons = new StringBuilder();
        sbCons.append(String.format("%-5s | %-16s | %-14s | %s\n", "ID", "Name", "Category", "Total Consumed (kW)"));
        sbCons.append("-----------------------------------------------------------------\n");
        for (Customer c : powerCompany.getCustomers()) {
            sbCons.append(String.format("%-5d | %-16s | %-14s | %.3f kW\n",
                    c.getId(), c.getName(), c.getType(), c.getTotalKwConsumed()));
        }
        consumptionTextArea.setText(sbCons.toString());

        // Update Issues List View
        StringBuilder sbIssues = new StringBuilder();
        for (MaintenanceIssue issue : powerCompany.getIssuesLog()) {
            sbIssues.append(issue.toString()).append("\n");
        }
        issuesTextArea.setText(sbIssues.toString());
        totalCostLabel.setText(String.format("Total Repair Expenses: $%.2f", powerCompany.getTotalRepairCost()));
    }


    /* 
     * Simple Gui Design
     * Let's keep this simple to avoid dealing with any GUI issues.
     *
     * I hate Java!
     */
    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(new Color(245, 245, 245));

        startButton = new JButton("Start Simulation Loop");
        stopButton = new JButton("Pause Simulation");
        JButton nextCycleButton = new JButton("Run Single Cycle");
        stopButton.setEnabled(false);

        cycleLabel = new JLabel("Cycle: 0");
        cycleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        topPanel.add(startButton);
        topPanel.add(stopButton);
        topPanel.add(nextCycleButton);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(cycleLabel);

        add(topPanel, BorderLayout.NORTH);

        // Center Split Layout
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.45);

        // Left Panel: Real-time Simulation Logs
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logTextArea);
        logScroll.setBorder(new TitledBorder("Live Operations & Transmission Log"));
        mainSplitPane.setLeftComponent(logScroll);

        // Right Panel: Summary Statistics
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        consumptionTextArea = new JTextArea();
        consumptionTextArea.setEditable(false);
        consumptionTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane consScroll = new JScrollPane(consumptionTextArea);
        consScroll.setBorder(new TitledBorder("Customer Consumption Overview"));

        issuesTextArea = new JTextArea();
        issuesTextArea.setEditable(false);
        issuesTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane issuesScroll = new JScrollPane(issuesTextArea);
        
        JPanel issuesContainer = new JPanel(new BorderLayout());
        issuesContainer.setBorder(new TitledBorder("Maintenance & Employee Dispatch Log"));
        issuesContainer.add(issuesScroll, BorderLayout.CENTER);

        totalCostLabel = new JLabel("Total Repair Expenses: $0.00");
        totalCostLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        totalCostLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        issuesContainer.add(totalCostLabel, BorderLayout.SOUTH);

        rightPanel.add(consScroll);
        rightPanel.add(issuesContainer);

        mainSplitPane.setRightComponent(rightPanel);
        add(mainSplitPane, BorderLayout.CENTER);

        // Timer setup (runs cycle every 1.5 seconds when started)
        simTimer = new Timer(1500, e -> runMonthlyCycle());

        // Event Handlers
        startButton.addActionListener(e -> {
            simTimer.start();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        });

        stopButton.addActionListener(e -> {
            simTimer.stop();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        });

        nextCycleButton.addActionListener(e -> runMonthlyCycle());

        updateSummaryViews();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            A35_PowerCompany app = new A35_PowerCompany();
            app.setVisible(true);
        });
    }
}
