package UI;
import Model.Appliance;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
public class LoadInputPanel {
    private static final long serialVersionUID = 1L;
    private final MainPage mainPage;

    private JTextField nameField, wattsField, qtyField, hoursField;

    // Modern color palette
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color PRIMARY_HOVER = new Color(37, 99, 235);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color SUCCESS_HOVER = new Color(22, 163, 74);
    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(71, 85, 105);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color INPUT_BORDER = new Color(203, 213, 225);

    // Auto-redirect option
    private JCheckBox autoRedirectCheckbox;
}
