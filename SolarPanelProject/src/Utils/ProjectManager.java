package Utils;

import Model.Appliance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import javax.swing.*;
import java.awt.Component;
import java.io.*;
import java.util.List;

public class ProjectManager {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Saves appliances AND solar parameters into a JSON file
     */
    public static void saveProject(Component parent, List<Appliance> appliances,
                                   double peakSunHours, double depthOfDischarge,
                                   int daysOfAutonomy, int systemVoltage) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Project As...");
        chooser.setSelectedFile(new File("SolarProject.json"));

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            
            // Ensure .json extension
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getAbsolutePath() + ".json");
            }
            
            try (Writer writer = new FileWriter(file)) {
                // Create project data with both appliances and solar parameters
                ProjectData projectData = new ProjectData(appliances, peakSunHours,
                        depthOfDischarge, daysOfAutonomy, systemVoltage);
                gson.toJson(projectData, writer);
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
     * Loads both appliances AND solar parameters from a JSON file
     */
    public static ProjectData loadProject(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Project File");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "JSON Files (*.json)", "json"));

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            
            // Validate file exists and is readable
            if (!file.exists()) {
                JOptionPane.showMessageDialog(parent,
                        "File does not exist: " + file.getAbsolutePath(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            if (!file.canRead()) {
                JOptionPane.showMessageDialog(parent,
                        "Cannot read file: " + file.getAbsolutePath(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            try (Reader reader = new FileReader(file)) {
                // Read and validate JSON content
                ProjectData projectData = gson.fromJson(reader, ProjectData.class);
                
                // Validate loaded data
                if (projectData == null) {
                    throw new IllegalStateException("File contains no valid data");
                }
                
                if (projectData.getAppliances() == null || projectData.getAppliances().isEmpty()) {
                    throw new IllegalStateException("File contains no appliances");
                }
                
                JOptionPane.showMessageDialog(parent,
                        "Project loaded successfully!\n" +
                                "Loaded " + projectData.getAppliances().size() + " appliances.",
                        "Load Successful", JOptionPane.INFORMATION_MESSAGE);


                return projectData;
                
            } catch (JsonSyntaxException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Invalid JSON format in file.\n" +
                                "The file may be corrupted or not a valid Solar Calculator project.\n\n" +
                                "Error: " + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to read project file:\n" + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Invalid project data:\n" + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent,
                        "Unexpected error loading project:\n" + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }
}