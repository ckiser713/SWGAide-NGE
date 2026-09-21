package swg.gui.schematics.craftsim;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import swg.crafting.schematics.SWGSchematic;
import swg.crafting.schematics.SWGExperimentGroup;
import swg.crafting.simulator.contracts.CoverageRecord;
import swg.crafting.simulator.contracts.EvidenceState;
import swg.gui.SWGFrame;
import swg.gui.schematics.SWGSchematicTab;
import swg.infinity.contracts.InfinityRuleset;
import swg.infinity.integration.InfinityGalaxyBinding;
import swg.infinity.integration.SchematicBinding;
import swg.infinity.integration.SchematicBindingRegistry;
import swg.model.SWGCGalaxy;

/**
 * Native Crafting Simulator tab inside {@link SWGSchematicTab}.
 *
 * <p>The tab wires the full runtime path:</p>
 * <ol>
 *   <li>reads {@link SWGFrame#getSelectedGalaxy()} and the active
 *       {@link InfinityRuleset} + {@link SchematicBindingRegistry};</li>
 *   <li>listens for {@link SWGSchematicTab#schematicSelect} and resolves
 *       the selected schematic to a runnable bound schematic via the
 *       registry;</li>
 *   <li>lets the operator pick a native resource scope (Current +
 *       Recent Set, Current Spawning, Inventory) and maps the
 *       selection into {@code ResourceSlotAssignment} entries via
 *       {@link NativeResourceAdapter};</li>
 *   <li>runs the resulting scenario through {@link SimEngineFacade},
 *       captures results for {@link SimEngineFacade#compare} and
 *       {@link SimEngineFacade#explain}, and computes a
 *       {@code MaterialPlan} via {@link SimEngineFacade#plan};</li>
 *   <li>on unsupported servers (NONE provider) the resource-only
 *       banner is rendered and final simulation is disabled.</li>
 * </ol>
 */
public final class SWGCraftingSimulatorTab extends JPanel {

    private static final long serialVersionUID = 1L;

    public static final String TAB_TITLE = "Crafting Simulator";
    public static final int TAB_MNEMONIC = java.awt.event.KeyEvent.VK_C;

    private final SWGSchematicTab owner;
    private final JLabel banner = new JLabel();
    private final JComboBox<String> resourceScope = new JComboBox<String>(
            new String[] {
                "Current + Recent Set",
                "Current Spawning",
                "Inventory" });
    private final DefaultListModel<String> schematicListModel =
            new DefaultListModel<String>();
    private final JList<String> schematicList = new JList<String>(
            schematicListModel);
    private final JPanel ingredientPanel = new JPanel();
    private final Map<Integer, JComboBox<
            swg.crafting.simulator.resources.ResourceSnapshot>>
            resourceSelectors =
                    new LinkedHashMap<Integer, JComboBox<
                            swg.crafting.simulator.resources.ResourceSnapshot>>();
    private final JTextField exoticComponentField = new JTextField(20);
    private final JSpinner recursionDepthSpinner = new JSpinner(
            new SpinnerNumberModel(1, 1, 8, 1));
    private final JComboBox<String> experimentGroupBox =
            new JComboBox<String>();
    private final JSpinner experimentPointsSpinner = new JSpinner(
            new SpinnerNumberModel(0, 0, 1000, 1));
    private final JSpinner craftCountSpinner = new JSpinner(
            new SpinnerNumberModel(1, 1, 100, 1));
    private final JTextArea resultArea = new JTextArea(8, 60);
    private final JTextArea compareArea = new JTextArea(8, 60);
    private final JTextArea explainArea = new JTextArea(8, 60);
    private final JTextArea materialsArea = new JTextArea(6, 60);

    private swg.crafting.simulator.scenario.CraftResult lastResultA;
    private swg.crafting.simulator.scenario.CraftResult lastResultB;

    private SWGSchematic selectedSchematic;
    private InfinityRuleset activeRuleset;
    private SchematicBindingRegistry activeRegistry;
    private SimEngineFacade.BoundSchematic lastBound;
    private swg.crafting.simulator.scenario.CraftScenario lastScenario;

    public SWGCraftingSimulatorTab(SWGSchematicTab owner) {
        this.owner = owner;
        setLayout(new BorderLayout());
        add(buildBanner(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildRunBar(), BorderLayout.SOUTH);
        refreshBanner();
    }

    /**
     * Sets the active ruleset + binding registry used by the runtime
     * path. The headless self-test supplies these directly; the GUI
     * obtains them from the registry that the SWGAide startup
     * bootstrap wires for server 154.
     */
    public void setRulesContext(
            InfinityRuleset ruleset, SchematicBindingRegistry registry) {
        this.activeRuleset = ruleset;
        this.activeRegistry = registry;
        refreshBanner();
    }

    public InfinityRuleset getActiveRuleset() { return activeRuleset; }
    public SchematicBindingRegistry getActiveRegistry() { return activeRegistry; }
    public SWGSchematic getSelectedSchematic() { return selectedSchematic; }
    public swg.crafting.simulator.scenario.CraftScenario getLastScenario() {
        return lastScenario;
    }
    public swg.crafting.simulator.scenario.CraftResult getLastResultA() {
        return lastResultA;
    }

    private JComponent buildBanner() {
        banner.setBorder(BorderFactory.createEtchedBorder());
        banner.setHorizontalAlignment(SwingConstants.LEFT);
        banner.setPreferredSize(new Dimension(0, 48));
        return banner;
    }

    private JComponent buildCenter() {
        JTabbedPane inner = new JTabbedPane();

        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel(" Schematic (manual fallback) "),
                BorderLayout.NORTH);
        schematicList.setVisibleRowCount(8);
        left.add(new JScrollPane(schematicList), BorderLayout.CENTER);

        JPanel resources = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resources.add(new JLabel("Resource scope:"));
        resources.add(resourceScope);
        resourceScope.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshBanner();
                refreshIngredientGrid();
            }
        });

        ingredientPanel.setLayout(new BoxLayout(ingredientPanel,
                BoxLayout.Y_AXIS));
        JPanel ingredients = new JPanel(new BorderLayout());
        ingredients.add(resources, BorderLayout.NORTH);
        ingredients.add(new JScrollPane(ingredientPanel),
                BorderLayout.CENTER);

        JPanel components = new JPanel(new FlowLayout(FlowLayout.LEFT));
        components.add(new JLabel("Exotic component (name):"));
        components.add(exoticComponentField);
        components.add(new JLabel("  Recursion depth:"));
        components.add(recursionDepthSpinner);

        JPanel experiment = new JPanel(new FlowLayout(FlowLayout.LEFT));
        experiment.add(new JLabel("Experiment group:"));
        experiment.add(experimentGroupBox);
        experiment.add(new JLabel("  Points:"));
        experiment.add(experimentPointsSpinner);

        JPanel craftCount = new JPanel(new FlowLayout(FlowLayout.LEFT));
        craftCount.add(new JLabel("Materials craft count:"));
        craftCount.add(craftCountSpinner);

        JPanel craft = new JPanel();
        craft.setLayout(new BoxLayout(craft, BoxLayout.Y_AXIS));
        craft.add(ingredients);
        craft.add(components);
        craft.add(experiment);
        craft.add(craftCount);

        JPanel leftAndCraft = new JPanel(new GridLayout(2, 1, 4, 4));
        leftAndCraft.add(left);
        leftAndCraft.add(craft);

        inner.addTab("Setup", leftAndCraft);
        inner.addTab("Result", scroll(resultArea));
        inner.addTab("Compare", scroll(compareArea));
        inner.addTab("Explain", scroll(explainArea));
        inner.addTab("Materials", scroll(materialsArea));
        return inner;
    }

    private JScrollPane scroll(JTextArea area) {
        area.setEditable(false);
        return new JScrollPane(area);
    }

    private JComponent buildRunBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton run = new JButton("Run craft");
        run.setMnemonic(java.awt.event.KeyEvent.VK_R);
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCraft(false);
            }
        });
        JButton runB = new JButton("Capture for compare");
        runB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCraft(true);
            }
        });
        JButton explain = new JButton("Explain last craft");
        explain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                explainLast();
            }
        });
        JButton materials = new JButton("Materials plan");
        materials.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                planMaterials();
            }
        });
        bar.add(run);
        bar.add(runB);
        bar.add(explain);
        bar.add(materials);
        return bar;
    }

    public void schemSelect(SWGSchematic s) {
        if (s == null) return;
        this.selectedSchematic = s;
        refreshIngredientGrid();
        refreshExperimentBox();
        refreshBanner();
    }

    private void refreshBanner() {
        SWGCGalaxy galaxy = SWGFrame.getSelectedGalaxy();
        StringBuilder sb = new StringBuilder();
        sb.append("<html>Server: ");
        sb.append(galaxy == null
                ? "(none)" : galaxy.getName());
        sb.append(" (id=");
        sb.append(galaxy == null
                ? "-" : galaxy.id());
        sb.append(")<br/>");
        sb.append("Rules provider: ");
        sb.append(activeRegistry == null
                ? "(none)" : activeRegistry.getServerId());
        sb.append("&nbsp;&nbsp;Ruleset SHA: ");
        sb.append(activeRuleset == null
                ? "(none)" : activeRuleset.getRulesetHash().substring(0, 16));
        sb.append("...&nbsp;&nbsp;Source commit: ");
        sb.append(activeRuleset == null
                ? "(none)" : activeRuleset.getManifest().getCommit().substring(0, 12));
        sb.append("...&nbsp;&nbsp;Coverage: ");
        if (selectedSchematic == null || activeRegistry == null
                || activeRuleset == null) {
            sb.append("(no schematic)");
        } else {
            SimEngineFacade.BoundSchematic bound =
                    SimEngineFacade.resolve(selectedSchematic,
                            activeRegistry, activeRuleset);
            lastBound = bound;
            if (bound.binding == null) {
                sb.append("MISSING");
            } else if (!bound.runnable) {
                sb.append(bound.binding.getState().name());
                sb.append(" (");
                sb.append(bound.binding.getEvidence());
                sb.append(")");
            } else {
                CoverageRecord cov = activeRuleset.getCoverage(
                        bound.definition.getId());
                EvidenceState overall = cov == null
                        ? EvidenceState.UNSUPPORTED : cov.overall();
                sb.append(overall.name());
                sb.append(" for ");
                sb.append(bound.definition.getId());
            }
        }
        sb.append("</html>");
        banner.setText(sb.toString());
    }

    private void refreshIngredientGrid() {
        ingredientPanel.removeAll();
        resourceSelectors.clear();

        if (selectedSchematic == null) {
            ingredientPanel.add(new JLabel(
                    "No schematic selected. Pick one above."));
            finishIngredientRefresh();
            return;
        }

        if (activeRegistry == null || activeRuleset == null) {
            ingredientPanel.add(new JLabel(
                    "No verified server rules context loaded."));
            finishIngredientRefresh();
            return;
        }

        SimEngineFacade.BoundSchematic bound =
                SimEngineFacade.resolve(
                        selectedSchematic, activeRegistry, activeRuleset);
        lastBound = bound;
        if (!bound.runnable || bound.definition == null) {
            ingredientPanel.add(new JLabel(
                    "Schematic binding is not runnable."));
            finishIngredientRefresh();
            return;
        }

        SWGCGalaxy galaxy = SWGFrame.getSelectedGalaxy();
        List<swg.crafting.simulator.resources.ResourceSnapshot> available =
                galaxy == null
                        ? Collections
                            .<swg.crafting.simulator.resources.ResourceSnapshot>
                                emptyList()
                        : NativeResourceAdapter.loadScope(
                                currentScope(), galaxy);

        List<swg.crafting.simulator.resources.ResourceCandidateSet> candidateSets =
                NativeResourceAdapter.candidates(bound.definition, available);
        Map<Integer, swg.crafting.simulator.resources.ResourceCandidateSet>
                bySlot =
                    new LinkedHashMap<Integer,
                        swg.crafting.simulator.resources.ResourceCandidateSet>();
        for (swg.crafting.simulator.resources.ResourceCandidateSet set
                : candidateSets) {
            bySlot.put(Integer.valueOf(set.getRequirement().getSlotIndex()), set);
        }

        for (swg.infinity.contracts.IngredientSlotDefinition slot
                : bound.definition.getSlots()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.add(new JLabel(
                    "Slot " + slot.getIndex()
                    + " " + slot.getTitle()
                    + " x" + slot.getQuantity()
                    + " [" + slot.getAcceptedType() + "]"));

            if (slot.getKind()
                    == swg.infinity.contracts.SlotKind.RESOURCE) {
                swg.crafting.simulator.resources.ResourceCandidateSet set =
                        bySlot.get(Integer.valueOf(slot.getIndex()));
                List<swg.crafting.simulator.resources.ResourceSnapshot> choices =
                        set == null
                                ? Collections
                                    .<swg.crafting.simulator.resources.ResourceSnapshot>
                                        emptyList()
                                : set.getCandidates();

                JComboBox<swg.crafting.simulator.resources.ResourceSnapshot> box =
                        new JComboBox<
                            swg.crafting.simulator.resources.ResourceSnapshot>(
                                choices.toArray(
                                    new swg.crafting.simulator.resources
                                        .ResourceSnapshot[choices.size()]));
                box.setPrototypeDisplayValue(
                        choices.isEmpty() ? null : choices.get(0));
                resourceSelectors.put(
                        Integer.valueOf(slot.getIndex()), box);
                row.add(box);
                if (choices.isEmpty()) {
                    row.add(new JLabel("No compatible resource"));
                }
            } else {
                row.add(new JLabel(
                        "Component slot — configure below / exact component input"));
            }
            ingredientPanel.add(row);
        }

        finishIngredientRefresh();
    }

    private void finishIngredientRefresh() {
        ingredientPanel.revalidate();
        ingredientPanel.repaint();
    }

    private void refreshExperimentBox() {
        experimentGroupBox.removeAllItems();
        if (selectedSchematic == null) return;
        for (SWGExperimentGroup g : selectedSchematic.getExperimentGroups()) {
            String label = g.getDescription();
            experimentGroupBox.addItem(label == null ? "(unnamed)" : label);
        }
    }

    private void runCraft(boolean captureForCompare) {
        if (selectedSchematic == null) {
            JOptionPane.showMessageDialog(this, "Select a schematic first.");
            return;
        }
        if (activeRegistry == null || activeRuleset == null) {
            JOptionPane.showMessageDialog(this,
                    "No active ruleset/binding registry.");
            return;
        }
        try {
            SimEngineFacade.BoundSchematic bound = SimEngineFacade.resolve(
                    selectedSchematic, activeRegistry, activeRuleset);
            lastBound = bound;
            if (!bound.runnable) {
                JOptionPane.showMessageDialog(this,
                        "Binding is " + bound.binding.getState().name()
                        + "; not runnable.");
                return;
            }
            List<swg.infinity.engine.ResourceSlotAssignment> resources =
                    gatherResources(bound.definition);
            String exo = exoticComponentField.getText();
            int recursion = (Integer) recursionDepthSpinner.getValue();
            List<swg.infinity.component.ComponentSlotAssignment> components =
                    SimEngineFacadeBridge.componentsFor(
                            exo, recursion, bound.definition);
            String group = (String) experimentGroupBox.getSelectedItem();
            int points = (Integer) experimentPointsSpinner.getValue();
            List<swg.crafting.simulator.scenario.ExperimentStep> steps =
                    SimEngineFacade.singleExperiment(
                            group, points,
                            swg.crafting.simulator.scenario.CraftOutcomeTier.GOOD);

            swg.crafting.simulator.scenario.CraftScenario scenario =
                    SimEngineFacade.buildScenario(
                            bound, resources, components,
                            swg.crafting.simulator.scenario.CraftOutcomeTier.GREAT,
                            steps);
            lastScenario = scenario;
            swg.crafting.simulator.scenario.CraftResult result =
                    SimEngineFacade.runExact(activeRuleset, scenario);
            resultArea.setText(SimEngineFacade.renderResult(result));
            if (captureForCompare) {
                if (lastResultA == null) {
                    lastResultA = result;
                    JOptionPane.showMessageDialog(this,
                            "Captured as build A.");
                } else {
                    lastResultB = result;
                    compareArea.setText(SimEngineFacade.renderCompare(
                            SimEngineFacade.compare(lastResultA, result)));
                    JOptionPane.showMessageDialog(this,
                            "Captured as build B; deltas computed.");
                }
            } else if (lastResultB == null) {
                lastResultA = result;
            } else {
                lastResultB = result;
                compareArea.setText(SimEngineFacade.renderCompare(
                        SimEngineFacade.compare(lastResultA, result)));
            }
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(this,
                    "Craft failed: " + t.getMessage());
        }
    }

    /**
     * Gathers the user-selected native resources for the bound schematic
     * using the current scope selector. Returns an empty list when the
     * galaxy has no resources in the chosen scope.
     */
    private List<swg.infinity.engine.ResourceSlotAssignment>
            gatherResources(swg.infinity.contracts.SchematicDefinition def) {
        Map<Integer, swg.crafting.simulator.resources.ResourceSnapshot>
                selections =
                    new LinkedHashMap<Integer,
                        swg.crafting.simulator.resources.ResourceSnapshot>();

        for (Map.Entry<Integer, JComboBox<
                swg.crafting.simulator.resources.ResourceSnapshot>> entry
                : resourceSelectors.entrySet()) {
            Object selected = entry.getValue().getSelectedItem();
            if (selected instanceof
                    swg.crafting.simulator.resources.ResourceSnapshot) {
                selections.put(
                        entry.getKey(),
                        (swg.crafting.simulator.resources.ResourceSnapshot)
                            selected);
            }
        }

        NativeResourceAdapter.Resolved resolved =
                NativeResourceAdapter.resolveSelections(def, selections);
        if (!resolved.warnings.isEmpty()) {
            StringBuilder warn = new StringBuilder(
                    "Resource selection warnings:\n");
            for (String w : resolved.warnings) {
                warn.append("  ").append(w).append('\n');
            }
            resultArea.setText(warn.toString());
        }
        return resolved.resources;
    }

    private NativeResourceAdapter.Scope currentScope() {
        String s = (String) resourceScope.getSelectedItem();
        if (s == null) return NativeResourceAdapter.Scope.CURRENT_AND_RECENT;
        if (s.startsWith("Current Spawning")) {
            return NativeResourceAdapter.Scope.CURRENT_SPAWNING;
        }
        if (s.startsWith("Inventory")) {
            return NativeResourceAdapter.Scope.INVENTORY;
        }
        return NativeResourceAdapter.Scope.CURRENT_AND_RECENT;
    }

    private void explainLast() {
        if (lastResultA == null || lastScenario == null) {
            JOptionPane.showMessageDialog(this, "Run a craft first.");
            return;
        }
        try {
            swg.crafting.simulator.explain.CraftExplanation exp =
                    SimEngineFacade.explain(lastScenario, lastResultA);
            explainArea.setText(SimEngineFacade.renderExplain(exp));
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(this,
                    "Explain failed: " + t.getMessage());
        }
    }

    private void planMaterials() {
        if (lastScenario == null) {
            JOptionPane.showMessageDialog(this,
                    "Run a craft first to compute materials.");
            return;
        }
        int n = (Integer) craftCountSpinner.getValue();
        swg.crafting.simulator.planning.MaterialPlan plan =
                SimEngineFacade.plan(lastScenario, n);
        StringBuilder sb = new StringBuilder(SimEngineFacade.renderPlan(plan));
        sb.append("Maximum craft count from current stock: ")
                .append(new swg.crafting.simulator.planning.MaterialPlanner()
                        .maximumCraftCount(lastScenario))
                .append('\n');
        materialsArea.setText(sb.toString());
    }

    static int _resolveGalaxyServerId(SWGCGalaxy galaxy) {
        return galaxy == null ? -1 : galaxy.id();
    }

    static boolean _isInfinityGalaxy(SWGCGalaxy galaxy) {
        return InfinityGalaxyBinding.isInfinity(galaxy);
    }
}
