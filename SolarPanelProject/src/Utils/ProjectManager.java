package Utils;

import Model.Appliance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.swing.*;

import java.awt.Component;
import java.io.*;
import java.util.List;

public class ProjectManager {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Saves all appliances into a JSON file chosen by the user.
     */
    public static void saveProject(Component parent, List<Appliance> appliances) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Project As...");
        chooser.setSelectedFile(new File("SolarProject.json"));

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(appliances, writer);
                JOptionPane.showMessageDialog(parent,
                        "Project saved successfully:\n" + file.getAbsolutePath(),
                        "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to save project: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Loads appliances from a JSON file chosen by the user.
     */
    public static List<Appliance> loadProject(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Project File");

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (Reader reader = new FileReader(file)) {
                Appliance[] loaded = gson.fromJson(reader, Appliance[].class);
                JOptionPane.showMessageDialog(parent,
                        "Project loaded successfully!",
                        "Load Successful", JOptionPane.INFORMATION_MESSAGE);
                return java.util.Arrays.asList(loaded);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to load project: " + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }
}
