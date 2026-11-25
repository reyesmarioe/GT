import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class A35_PowerCompany extends JFrame {

    /*
     * Config file reader
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConfigReader {
        @JsonProperty("CompanyName")
        private String companyName;

        @JsonProperty("Owners")
        private List<String> owners;

        @JsonProperty("Consumers")
        private Map<String, Integer> consumers;

        @JsonProperty("ElectricityEvents")
        private Map<String, Double> electricityEvents;

        @JsonProperty("Employees")
        private List<String> employees;

        @JsonProperty("SystemEvents")
        private Map<String, Integer> systemEvents;

        @JsonProperty("ElectricEventsPerCycle")
        private int electricEventsPerCycle;

        @JsonProperty("EventsLowConsumer")
        private int eventsLowConsumer;

        @JsonProperty("EventsHighConsumer")
        private int eventsHighConsumer;

        public String getCompanyName() { 
            return companyName; 
        }
        public List<String> getOwners() { 
            return owners; 
        }
        public Map<String, Integer> getConsumers() { 
            return consumers; 
        }
        public Map<String, Double> getElectricityEvents() { 
            return electricityEvents; 
        }
        public List<String> getEmployees() { 
            return employees; 
        }
        public Map<String, Integer> getSystemEvents() { 
            return systemEvents; 
        }
        public int getElectricEventsPerCycle() { 
            return electricEventsPerCycle; 
        }
        public int getEventsLowConsumer() { 
            return eventsLowConsumer; 
        }
        public int getEventsHighConsumer() { 
            return eventsHighConsumer; 
        }
    }

    // TODO: 
    // Will rely on config file instead of hardcoding here
    public enum CustomerType { LOW_CONSUMER, HIGH_CONSUMER }

    public static class ApplianceEvent {
        private final String name;
        private final double kwUsage;

        public ApplianceEvent(String name, double kwUsage) {
            this.name = name;
            this.kwUsage = kwUsage;
        }

        public String getName() { 
            return name; 
        }
        public double getKwUsage() { 
            return kwUsage; 
        }
    }

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

    public static class Transformer {
        private final int id;
        public Transformer(int id) { 
            this.id = id; 
        }
        public int getId() { 
            return id; 
        }
    }

    public static class Substation {
        private final int id;
        private final List<Transformer> transformers = new ArrayList<>();
        /*
         * TODO:
         * Remove this and use value from config.json
         */
        public static final int MAX_TRANSFORMERS = 20;

        public Substation(int id) {
            this.id = id;
            for (int i = 1; i <= MAX_TRANSFORMERS; i++) {
                transformers.add(new Transformer(i));
            }
        }

        public int getId() { 
            return id; 
        }
        public List<Transformer> getTransformers() { 
            return transformers; 
        }
    }

    public static class PowerStation {
        private final String name;
        private final List<Substation> substations = new ArrayList<>();

        public PowerStation(String name) {
            this.name = name;
            substations.add(new Substation(1));
            substations.add(new Substation(2));
        }

        public String getName() { 
            return name; 
        }
        public List<Substation> getSubstations() { 
            return substations; 
        }
    }

    public static class Employee {
        private final int id;
        private final String name;

        public Employee(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getName() { 
            return name; 
        }
    }

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

        public double getFixCost() { 
            return fixCost; 
        }

        @Override
        public String toString() {
            return String.format("[Cycle %d] %s | Assigned to: %s | Time to fix: %d mins | Cost: $%.2f",
                    cycle, issueType, assignedWorker.getName(), fixTimeMinutes, fixCost);
        }
    }

    public static class PowerCompany {
        private final String name;
        private final ConfigReader config;
        private final PowerStation powerStation;
        private final List<Customer> customers = new ArrayList<>();
        private final List<Employee> employees = new ArrayList<>();
        private final List<MaintenanceIssue> issuesLog = new ArrayList<>();

        /*
         * This is the main class start
         */
        public PowerCompany(ConfigReader config) {
            this.config = config;
            this.name = config.getCompanyName(); 

            // Remove this hardcode name
            this.powerStation = new PowerStation("Station 1");

            // Create Employees
            //
            initEmployees();
            // create customers.
            initCustomers();
        }

        private void initEmployees() {
                // We should remove this hardcoded value. add to config
                // file
                int id = 20;
                
                // Total of employees retrieved from config file.
                for (String employeeName : config.getEmployees()) {
                    employees.add(new Employee(id++, employeeName));
                }
            }

        private void initCustomers() {
            int id = 1;

            // Customers are classified as Low and High consumers.
            for (Map.Entry<String, Integer> entry : config.getConsumers().entrySet()) {
                CustomerType type = (entry.getValue() == 1) ? CustomerType.HIGH_CONSUMER : CustomerType.LOW_CONSUMER;
                customers.add(new Customer(id++, entry.getKey(), type));
            }
        }

        /*
         * Each customer can generate different electricity events.
         * We'll use this to randomize the number of events for each customer
         */
        public int randomizeEventsPerCustomer(CustomerType customerType) {
            Random rand = new Random();
            int eventsPerCycle = config.getElectricEventsPerCycle() / config.getConsumers().size(); 
            int randomEvents;


            System.out.printf("Total events per cycle: %d\n",  config.getElectricEventsPerCycle());
            System.out.printf("Total consumers : %d\n", config.getConsumers().size());
            System.out.printf("Total events per customer: %d\n", eventsPerCycle);
            // If LOW_CONSUMER, restrict the number of events to just a few
            if (customerType == CustomerType.LOW_CONSUMER) {
                randomEvents = rand.nextInt(config.getEventsLowConsumer()) + 1;
            } else {
                randomEvents = rand.nextInt(config.getEventsHighConsumer()) + 1;
            }
            System.out.printf("Random events per customer: %d\n", randomEvents);
            return randomEvents;
        }

        public ConfigReader getConfig() { 
            return config; 
        }
        public String getName() { 
            return name; 
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

    private final ConfigReader config;
    private final PowerCompany powerCompany;
    private final List<ApplianceEvent> applianceEvents = new ArrayList<>();
    private final Random random = new Random();
    private int currentCycle = 0;
    private Timer appRunTimer;

    private JTextArea logTextArea;
    private JTextArea consumptionTextArea;
    private JTextArea issuesTextArea;
    private JLabel cycleLabel;
    private JLabel totalCostLabel;
    private JButton startButton;
    private JButton stopButton;

    public A35_PowerCompany(ConfigReader config) {
        super(config.getCompanyName());
        this.config = config;
        this.powerCompany = new PowerCompany(config);
        initApplianceEvents();
        setupUI();
    }

    private void initApplianceEvents() {
            config.getElectricityEvents().forEach((eventName, kw) -> {
                applianceEvents.add(new ApplianceEvent(eventName, kw));
            });
    }

    private void runMonthlyCycle() {
        currentCycle++;
        logTextArea.append("=== STARTING CYCLE " + currentCycle + " ===\n");

        Substation sub = powerCompany.getPowerStation().getSubstations().get(0);
        Transformer trans = sub.getTransformers().get(0);
        logTextArea.append(String.format("Power Grid Transmission: [%s] -> [Substation #%d] -> [Transformer #%d] -> [Grid]\n",
                    powerCompany.getPowerStation().getName(), sub.getId(), trans.getId()));

        /* 
         * Not all customers will use electricity this cycle
         * We can modify this and have all customers use electricity
         * in the cycle
         */
        int activeCustomersCount = random.nextInt(powerCompany.getCustomers().size()) + 1;

        /*
         * This is the total electricity events that can occurred in a cycle.
         * One customer will trigger more than one event.
         * The total of events from all customers will not exceed the total 
         * electricity events.
         */
        for (int i = 0; i < activeCustomersCount; i++) {
            Customer c = powerCompany.getCustomers().get(random.nextInt(powerCompany.getCustomers().size()));
            int eventsPerCustomer = powerCompany.randomizeEventsPerCustomer(c.getType());

            /*
             * Need to create several events per customer
             */
            do {
                ApplianceEvent event = applianceEvents.get(random.nextInt(applianceEvents.size()));

                double multiplier = (c.getType() == CustomerType.HIGH_CONSUMER) ? 2.5 : 1.0;
                double kwUsed = event.getKwUsage() * multiplier;
                c.consume(kwUsed);

                logTextArea.append(String.format("  - %s (%s) used '%s' consuming %.3f kW\n",
                            c.getName(), c.getType(), event.getName(), kwUsed));
            }while (eventsPerCustomer-- > 0);
        }

        // Create random electricity events/failures or not.
        if (random.nextBoolean()) {
            List<String> systemEvents = new ArrayList(config.getSystemEvents().keySet());
            String event = systemEvents.get(random.nextInt(systemEvents.size()));

            double issueCost = config.getSystemEvents().get(event).doubleValue(); 

            // This is just a random fix time, but shortest time is 15 min.
            // Maybe we should increase this value?
            // Overall, it does not affect the performance of the program
            int fixTime = random.nextInt(120) + 15;

            Employee assigned = powerCompany.getEmployees().get(random.nextInt(powerCompany.getEmployees().size()));

            // Create the maintenance issue.
            MaintenanceIssue issue = new MaintenanceIssue(currentCycle, event, fixTime, issueCost, assigned);
            powerCompany.logIssue(issue);
            logTextArea.append("  Power System Issue: " + issue + "\n");
        } else {
            logTextArea.append("  Grid Status: No issues reported.\n");
        }


        /* 
         * Add electricity sale activity here
         *
         * How to do determine how much electricity each user has?
         * Do we set a maximum number of kw per user and if low users dont reach that
         * limit they can sell what's remaining?
         * I think this would be the best approach.
         *
         * So the calculation would be something like:
         *
         * Tier1    1000kw @ 1.00
         * Tier2    1000kw @ 2.00
         * Tier3           @ 4.00
         *
         * LOW USER
         *      Limit 1000 kw.
         *      Uses 400 kw @ 1.00/kw
         *      Sells 600 kw @ 1.00/kw
         *      
         * HIGH USER
         *      Uses 1800 kw (1000kw * 1.00) + (800kw @ 2.00)
         *      However, it buys 600kw @ 1.00 from LOW USER.
         *      Pays 200kw @ 2.00
         *      Total monthly pay is 2000.00
         *
         *
         */
        

        logTextArea.append("\n");
        updateSummaryViews();
    }

    private void updateSummaryViews() {
        cycleLabel.setText("Cycle: " + currentCycle);

        StringBuilder sbCons = new StringBuilder();
        sbCons.append(String.format("%-5s | %-16s | %-14s | %s\n", "ID", "Name", "Category", "Total Consumed (kW)"));
        sbCons.append("-----------------------------------------------------------------\n");
        for (Customer c : powerCompany.getCustomers()) {
            sbCons.append(String.format("%-5d | %-16s | %-14s | %.3f kW\n",
                    c.getId(), c.getName(), c.getType(), c.getTotalKwConsumed()));
        }
        consumptionTextArea.setText(sbCons.toString());

        StringBuilder sbIssues = new StringBuilder();
        for (MaintenanceIssue issue : powerCompany.getIssuesLog()) {
            sbIssues.append(issue.toString()).append("\n");
        }
        issuesTextArea.setText(sbIssues.toString());
        totalCostLabel.setText(String.format("Total Repair Expenses: $%.2f", powerCompany.getTotalRepairCost()));
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(new Color(245, 245, 245));

        startButton = new JButton("Start Cycle Loop");
        stopButton = new JButton("Pause Cycle Loop");
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

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.45);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logTextArea);
        logScroll.setBorder(new TitledBorder("Current Electricity Trends"));
        mainSplitPane.setLeftComponent(logScroll);

        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        consumptionTextArea = new JTextArea();
        consumptionTextArea.setEditable(false);
        consumptionTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane consScroll = new JScrollPane(consumptionTextArea);
        consScroll.setBorder(new TitledBorder("Customer Consumption"));

        issuesTextArea = new JTextArea();
        issuesTextArea.setEditable(false);
        issuesTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane issuesScroll = new JScrollPane(issuesTextArea);

        JPanel issuesContainer = new JPanel(new BorderLayout());
        issuesContainer.setBorder(new TitledBorder("Grid Events"));
        issuesContainer.add(issuesScroll, BorderLayout.CENTER);

        totalCostLabel = new JLabel("Total Repair Expenses: $0.00");
        totalCostLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        totalCostLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        issuesContainer.add(totalCostLabel, BorderLayout.SOUTH);

        rightPanel.add(consScroll);
        rightPanel.add(issuesContainer);

        mainSplitPane.setRightComponent(rightPanel);
        add(mainSplitPane, BorderLayout.CENTER);

        appRunTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runMonthlyCycle();
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appRunTimer.start();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appRunTimer.stop();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });

        nextCycleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runMonthlyCycle();
            }
        });

        updateSummaryViews();
    }

    public static void main(String[] args) {
        String configFile = "config.json";
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ConfigReader parsedConfig = null;
        try {
            parsedConfig = mapper.readValue(new File(configFile), ConfigReader.class);
            System.out.println("Loaded Company: " + parsedConfig.getCompanyName());
        } catch (IOException e) {
            System.err.println("Could not read config.json, using defaults.");
            e.printStackTrace();
        }

        final ConfigReader config = parsedConfig;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                A35_PowerCompany app = new A35_PowerCompany(config);
                app.setVisible(true);
            }
        });
    }
}

