/**
 * This java program automates various aspects of the "Granblue Fantasy Relink" Game, 
 * enabling unlimited AFK farming of resources, even from the hardest content.
 * 
 * The program uses the java.awt.Robot library to send native mouse clicks and
 * key strokes, as well as detect on-screen pixels to understand game states.
 * The program does not hook into any of the game's processes nor modify the game's files.
 * Thus, the program is safe to use and conventional anti-bot programs can not detect its presence.
 * 
 * The program is not considered a "cheat" since it does not provide unfair advantage to players
 * during gameplay. Even though the program can execute most actions that a player can do, it
 * does not understand what's going on, so a player should be able to out perform the program
 * during combat. The program repeats a scripted set of actions, as seen in the "actionTimerAction()" function.
 * 
 * The goal of the program is to save time for players, considering the grindy nature of the game. 
 * 
 * @author: Giga-D-Y
 * @version: 2.4
 * @created: 2024-02-20
 * @last_updated: 2024-03-03
 */

/* Library imports */
import javax.swing.*;
import java.util.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.beans.PropertyChangeListener;

/* Main class declaration */
public class GBFR_Auto_Pilot
{
    /* Object declarations */
    Robot robot;
    Random rand;
    javax.swing.Timer actionT;
    javax.swing.Timer pointerT;
    JFrame mainFrame;    
    JTextArea mainInstructionTextArea;
    JButton afkFarmButton;
    JCheckBox moveForwardCheckBox;
    JCheckBox tapAttackCheckBox;
    JCheckBox holdAttackCheckBox;
    JCheckBox usePotionCheckBox;
    JCheckBox useLinkAttackCheckBox;
    JCheckBox useSBACheckBox;
    JCheckBox useTargetingCheckBox;
    JCheckBox panCameraCheckBox;    
    JCheckBox optimizeforSlimepedeCheckBox;
    JCheckBox enableAdvancedOptionsCheckBox;
    JCheckBox useCombosCheckBox;
    JCheckBox useSkillsCheckBox;
    JCheckBox holdGuardCheckBox;
    JCheckBox keepDodgingCheckBox;
    JPanel screenDetectionPixelCapturePanel;
    JDialog screenDetectionPixelCaptureDialog;
    JLabel screenDetectionPointerLabel;
    JTextArea screenDetectionInstructionTextArea;
    JButton missionResultPixelCaptureButton;
    JButton healthBarPixelCaptureButton;
    
    /* UI variables */
    double screenWidth;
    double screenHeight;
    double windowWidth;
    double windowHeight;
    double windowLocationX;
    double windowLocationY;
    int dialogWidth;
    int dialogHeight;
    
    /* Enums */
    enum AutoPilotFunctions {
        NOT_SET,
        AFK_FARM,
        OTHER
    }
    
    /* Static values */
    static final int ROBOT_DEFAULT_AUTO_DELAY = 100;    
    static final String MAIN_FRAME_INSTRUCTIONS = " 1. Removes the 10-mission auto repeat limit.\n" +         
        " 2. [Optional] Auto moves forward (keybind: W)\n" + 
        " 3. [Optional] Auto light attacks (keybind: left click)\n" + 
        " 4. [Optional] Use link attacks (keybind: R)\n" + 
        " 5. [Optional] Use SBAs (keybind: G)\n" + 
        " 6. [Optional] Holds guard (keybind: Q)\n" + 
        " 7. [Optional] Keeps dodging (keybind: E)\n" + 
        " 8. [Optional] Auto targets (keybind: middle click)\n" + 
        " 9. [Optional] Auto pans camera ~180 degrees (moves your cursor)\n" + 
        " 10. [Optional] Uses combo finishers (keybind: right click)\n" + 
        " 11. [Optional] Uses skills in rotation (keybinds: T for skill button, 1-4 for each skill)\n" + 
        " 12. [Experimental] Auto drinks potions when health is low (including revival)\n" + 
        " (Potion keybinds: Z = Mega, X = Green, C = Blue, V = Revival)\n" + 
        " The program assumes you have the following number of potions:\n" + 
        " Mega: 6, Green: 8, Blue: 6, Revival: 3\n" + 
        " 13. [Requires health bar pixel] Attempts to recover faster from critical (keybind: Space)\n" +
        "\n * Don't forget to do 1 run and turn on repeat, before turning on auto pilot.\n" + 
        "\n ** If doing prism slimepede farm, recommend using Eugen, Io, or Rackam.\n" + 
        " ~100k light attack is needed to one-shot a slime.\n" + 
        " A normal-damage-capped Eugen is highly recommended for this farm.\n";
    
    static final String SCREEN_DETECTION_INSTRUCTIONS = " To capture the mission result pixel: \n" + 
        " 1. On the mission result screen, move cursor on top of the \"Mission Result\" wording on top left (get the golden part).\n" + 
        " 2. Press SHIFT+Z on the keyboard, while this program has focus, to record the location and color of the pixel.\n" +
        " The mission result pixel is used to determine whether the mission result screen is showing.\n" + 
        "\n To capture the health bar pixel: \n" + 
        " 1. During the battle, move cursor on top of your health bar, at a threshold\n" + 
        " where you would want to drink potions if your health were to fall under. (recommend around 70% threshold)\n" + 
        " 2. Press SHIFT+X on the keyboard, while this program has focus, to record the location of the pixel.\n" +
        " The health bar pixel is used to determine whether drinking health potion is needed.\n" +         
        "\n * Do not click on the game window when trying to capture the pixel.\n" +
        " ** If the capture is successful, you will see the \"Pixel Captured!\" message at the bottom of this window.\n" + 
        " *** If you skip these steps, the advanced features may malfunction or cause the program to miss the 10-mission auto repeat pop up.";
        
    static final String START_AFK_FARM_BUTTON_TEXT = "Start AFK Farm - press here and then click the game window";
    static final String AFK_FARM_RUNNING_BUTTON_TEXT = "AFK Farm running - move your mouse around to stop";
    static final String MISSION_RESULT_PIXEL_CAPTURE_BUTTON_TEXT = "SHIFT+Z, while this has focus, to capture Mission Result pixel at the cursor";
    static final String MISSION_RESULT_PIXEL_RE_CAPTURE_BUTTON_TEXT = "Mission Result Pixel Captured! | Click here to re-capture";
    static final String HEALTH_BAR_PIXEL_CAPTURE_BUTTON_TEXT = "SHIFT+X, while this has focus, to capture Health Bar pixel at the cursor";
    static final String HEALTH_BAR_PIXEL_RE_CAPTURE_BUTTON_TEXT = "Health Bar Pixel Captured! | Click here to re-capture";
    static final Color missionResultPopUpColor = new Color(49, 76, 129); // This is the standard color for the pop up that shows during mission results.

    /* Global variables
     * 
     * To the extent possible, this program globalizes variables instead of using local variables.
     * This is because most workflows in the program are repeatedly fired from Timers, and
     * relying too much on Java's garbage collection could result in memory leak or CPU slowdowns.
     */
    int i = 0;
    AutoPilotFunctions functionSelector = AutoPilotFunctions.NOT_SET;
    int previousMousePosX = -1;
    int previousMousePosY = -1;
    int mouseMovedThreshold = 10;
    int numLeftClicks = -1;
    int numRightClicks = -1;
    int numSkillClicks = -1;
    int skillRotation = -1;
    Color currentScreenPixelColor = null;
    int currentScreenPixelX = -1;
    int currentScreenPixelY = -1;
    int currentScreenPixelR = -1;
    int currentScreenPixelG = -1;
    int currentScreenPixelB = -1;
    Color screenDetectionPointerLabelTextColor = null;
    int r_foreground = -1;
    int g_foreground = -1;
    int b_foreground = -1;
    Color missionResultPixelColor = null;
    int missionResultPixelX = -1;
    int missionResultPixelY = -1;
    int missionResultPixelR = -1;
    int missionResultPixelG = -1;
    int missionResultPixelB = -1;
    boolean missionResultPixelRecordingOn = false;
    Color healthBarPixelColor = null;
    int healthBarPixelX = -1;
    int healthBarPixelY = -1;
    int healthBarPixelR = -1;
    int healthBarPixelG = -1;
    int healthBarPixelB = -1;
    boolean healthBarPixelRecordingOn = false;    
    Color comparisonPixelColor = null;
    int comparisonPixelR = -1;
    int comparisonPixelG = -1;
    int comparisonPixelB = -1;    
    int diff_r_g_comparison = -1;
    int diff_g_b_comparison = -1;
    int diff_r_g_missionResult = -1;
    int diff_g_b_missionResult = -1;
    int diff_r_g_healthBar = -1;
    int diff_g_b_healthBar = -1;
    int missionResultPixelComparisonThreshold = -1;
    int healthBarPixelComparisonThreshold = -1;
    int numGreenPotion = -1;
    int numBluePotion = -1;
    int numMegaPotion = -1;
    int numRevivalPotion = -1;
    
    /* Constructor */
    public GBFR_Auto_Pilot() {
        
        
        rand = new Random();
        
        try {
            robot = new Robot();
            //robot.setAutoDelay(rand.nextInt(200) + 100);
            robot.setAutoDelay(ROBOT_DEFAULT_AUTO_DELAY);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("---Terminated---");
            return;
        }
        
        initTimers();
        initInterface();
        selectDefaultUIOptions();
    }
    
    public void initTimers() {
        actionT = new javax.swing.Timer(100, new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                actionTimerAction();
            }            
        });
        actionT.setInitialDelay(3000);
        
        pointerT = new javax.swing.Timer(50, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                updateCurrentScreenPixel();
            }
        });
    }
    
    public void initInterface() {
        screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        
        // Set the size of the main UI window here.
        windowWidth = 770;
        windowHeight = 420;
        
        // Set the size of the pop up dialog here.
        dialogWidth = 750;
        dialogHeight = 340;
        
        // Position the main UI window
        windowLocationX = screenWidth - Math.max(windowWidth, dialogWidth) - 10;
        windowLocationY = screenHeight / 2 - windowHeight / 2 + dialogHeight / 2;
        
        /* Construct the main UI window and its components. */
        mainFrame = new JFrame();
        mainFrame.setTitle("GBFR_Auto_Pilot For AFK Farming V2.4 - by Giga");
        mainFrame.setSize((int)windowWidth,(int)windowHeight);
        mainFrame.setLocation((int)windowLocationX, (int)windowLocationY);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setAlwaysOnTop(true);
        
        mainInstructionTextArea = new JTextArea(MAIN_FRAME_INSTRUCTIONS);
        mainInstructionTextArea.setEditable(false);
        mainFrame.add(mainInstructionTextArea, BorderLayout.CENTER);
        // Add key listener to the TextArea on the MainFrame, 
        // in order to respond to the KeyStroke for recording the screenDetectionPixel.
        int mainInstructionTextAreaKeyActionCondition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap mainInstructionTextAreaInputMap = mainInstructionTextArea.getInputMap(mainInstructionTextAreaKeyActionCondition);
        ActionMap mainInstructionTextAreaActionMap = mainInstructionTextArea.getActionMap(); 
        mainInstructionTextAreaInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK), "VK_SHIFT_Z");
        mainInstructionTextAreaActionMap.put("VK_SHIFT_Z", new Action(){
            public void actionPerformed(ActionEvent e) {
                if (screenDetectionPixelCaptureDialog.isVisible() && missionResultPixelRecordingOn) {
                    recordMissionResultPixel();
                }
            }
            public boolean isEnabled() {return true;}
            public void setEnabled(boolean b) {}
            public void putValue(String key, Object value) {}
            public Object getValue(String key) {return new Object();}
            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        });
        mainInstructionTextAreaInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.SHIFT_DOWN_MASK), "VK_SHIFT_X");
        mainInstructionTextAreaActionMap.put("VK_SHIFT_X", new Action(){
            public void actionPerformed(ActionEvent e) {
                if (screenDetectionPixelCaptureDialog.isVisible() && healthBarPixelRecordingOn) {
                    recordHealthBarPixel();
                }
            }
            public boolean isEnabled() {return true;}
            public void setEnabled(boolean b) {}
            public void putValue(String key, Object value) {}
            public Object getValue(String key) {return new Object();}
            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        });
        
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(17, 1));
        mainFrame.add(optionsPanel, BorderLayout.EAST);
        
        JLabel combatOptionsLabel = new JLabel("Combat Options");
        combatOptionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        combatOptionsLabel.setOpaque(true);
        combatOptionsLabel.setBackground(Color.LIGHT_GRAY);
        optionsPanel.add(combatOptionsLabel);
        
        moveForwardCheckBox = new JCheckBox("Move forward");
        moveForwardCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(moveForwardCheckBox);
        
        tapAttackCheckBox = new JCheckBox("Tap attacks and skills");
        tapAttackCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(tapAttackCheckBox);
        
        holdAttackCheckBox = new JCheckBox("(Experimental) Hold attacks and skills");
        holdAttackCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(holdAttackCheckBox);
        
        useLinkAttackCheckBox = new JCheckBox("Use link attacks");
        useLinkAttackCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(useLinkAttackCheckBox);
        
        useSBACheckBox = new JCheckBox("Use SBA");
        useSBACheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(useSBACheckBox);
        
        holdGuardCheckBox = new JCheckBox("Hold guard");
        holdGuardCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(holdGuardCheckBox);
        
        keepDodgingCheckBox = new JCheckBox("Keep dodging");
        keepDodgingCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(keepDodgingCheckBox);
        
        JLabel cameraOptionsLabel = new JLabel("Camera Options");
        cameraOptionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cameraOptionsLabel.setOpaque(true);
        cameraOptionsLabel.setBackground(Color.LIGHT_GRAY);
        optionsPanel.add(cameraOptionsLabel);
        
        useTargetingCheckBox = new JCheckBox("Use targeting");
        useTargetingCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(useTargetingCheckBox);
        
        panCameraCheckBox = new JCheckBox("Pan camera");
        panCameraCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(panCameraCheckBox);
        
        JLabel advancedOptionsLabel = new JLabel("Advanced Options");
        advancedOptionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        advancedOptionsLabel.setOpaque(true);
        advancedOptionsLabel.setBackground(Color.LIGHT_GRAY);
        optionsPanel.add(advancedOptionsLabel);
        
        optimizeforSlimepedeCheckBox = new JCheckBox("Optimize for prism Slimepede farm");
        optimizeforSlimepedeCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(optimizeforSlimepedeCheckBox);
        
        enableAdvancedOptionsCheckBox = new JCheckBox("Enable Advanced Features");
        enableAdvancedOptionsCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        optionsPanel.add(enableAdvancedOptionsCheckBox);
        
        JPanel useCombosPanel = new JPanel();
        useCombosPanel.setLayout(new BorderLayout());
        optionsPanel.add(useCombosPanel);        
        JLabel emptyLabelForUseCombos = new JLabel("    ");
        useCombosPanel.add(emptyLabelForUseCombos, BorderLayout.WEST);        
        useCombosCheckBox = new JCheckBox("Use combos");
        useCombosCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        useCombosCheckBox.setEnabled(false);
        useCombosPanel.add(useCombosCheckBox, BorderLayout.CENTER);
        
        JPanel useSkillsPanel = new JPanel();
        useSkillsPanel.setLayout(new BorderLayout());
        optionsPanel.add(useSkillsPanel);
        JLabel emptyLabelForUseSkills = new JLabel("    ");
        useSkillsPanel.add(emptyLabelForUseSkills, BorderLayout.WEST);
        useSkillsCheckBox = new JCheckBox("Use skills");
        useSkillsCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        useSkillsCheckBox.setEnabled(false);
        useSkillsPanel.add(useSkillsCheckBox, BorderLayout.CENTER);
        
        JPanel usePotionPanel = new JPanel();
        usePotionPanel.setLayout(new BorderLayout());
        optionsPanel.add(usePotionPanel);
        JLabel emptyLabelForUsePotion = new JLabel("    ");
        usePotionPanel.add(emptyLabelForUsePotion, BorderLayout.WEST);
        usePotionCheckBox = new JCheckBox("(Experimental) Use potions");
        usePotionCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                checkBoxClicked(e);
            }
        });
        usePotionCheckBox.setEnabled(false);
        usePotionPanel.add(usePotionCheckBox, BorderLayout.CENTER);
       
        JPanel buttonsPanel = new JPanel();  
        buttonsPanel.setLayout(new GridLayout(1, 1));
        mainFrame.add(buttonsPanel, BorderLayout.SOUTH);
        
        afkFarmButton = new JButton(START_AFK_FARM_BUTTON_TEXT);
        afkFarmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                afkFarmButtonPressed();
            }            
        });
        afkFarmButton.setBackground(Color.GREEN);
        buttonsPanel.add(afkFarmButton);
        
        /* Construct the Screen Detection Pixel pop up dialog UI and its components */
        screenDetectionPixelCapturePanel = new JPanel();
        screenDetectionPixelCapturePanel.setLayout(new BorderLayout());
        // Add key listener to the pop up screen used to capture the screenDetectionPixel, 
        // in order to respond to the KeyStroke for recording the screenDetectionPixel.
        int screenDetectionPixelCapturePanelKeyActionCondition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap screenDetectionPixelCapturePanelInputMap = screenDetectionPixelCapturePanel.getInputMap(screenDetectionPixelCapturePanelKeyActionCondition);
        ActionMap screenDetectionPixelCapturePanelActionMap = screenDetectionPixelCapturePanel.getActionMap(); 
        screenDetectionPixelCapturePanelInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK), "VK_SHIFT_Z");
        screenDetectionPixelCapturePanelActionMap.put("VK_SHIFT_Z", new Action(){
            public void actionPerformed(ActionEvent e) {
                if (screenDetectionPixelCaptureDialog.isVisible() && missionResultPixelRecordingOn) {
                    recordMissionResultPixel();
                }
            }
            public boolean isEnabled() {return true;}
            public void setEnabled(boolean b) {}
            public void putValue(String key, Object value) {}
            public Object getValue(String key) {return new Object();}
            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        });
        screenDetectionPixelCapturePanelInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.SHIFT_DOWN_MASK), "VK_SHIFT_X");
        screenDetectionPixelCapturePanelActionMap.put("VK_SHIFT_X", new Action(){
            public void actionPerformed(ActionEvent e) {
                if (screenDetectionPixelCaptureDialog.isVisible() && healthBarPixelRecordingOn) {
                    recordHealthBarPixel();
                }
            }
            public boolean isEnabled() {return true;}
            public void setEnabled(boolean b) {}
            public void putValue(String key, Object value) {}
            public Object getValue(String key) {return new Object();}
            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        });
        
        screenDetectionPointerLabel = new JLabel("Cursor Coordinates: 0, 0 | Pixel Color: r, g, b");
        screenDetectionPointerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        screenDetectionPointerLabel.setOpaque(true);
        screenDetectionPointerLabel.setBackground(Color.LIGHT_GRAY);
        screenDetectionPixelCapturePanel.add(screenDetectionPointerLabel, BorderLayout.NORTH);
        
        screenDetectionInstructionTextArea = new JTextArea(SCREEN_DETECTION_INSTRUCTIONS);
        screenDetectionInstructionTextArea.setEditable(false);
        screenDetectionPixelCapturePanel.add(screenDetectionInstructionTextArea, BorderLayout.CENTER);
        
        JPanel screenDetectionPixelCaptureButtonsPanel = new JPanel();
        screenDetectionPixelCaptureButtonsPanel.setLayout(new GridLayout(2, 1));
        screenDetectionPixelCapturePanel.add(screenDetectionPixelCaptureButtonsPanel, BorderLayout.SOUTH);
        
        missionResultPixelCaptureButton = new JButton(MISSION_RESULT_PIXEL_CAPTURE_BUTTON_TEXT);
        missionResultPixelCaptureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetMissionResultPixel();
            }            
        });
        missionResultPixelCaptureButton.setEnabled(false);
        missionResultPixelCaptureButton.setBackground(Color.WHITE);
        screenDetectionPixelCaptureButtonsPanel.add(missionResultPixelCaptureButton);
        
        healthBarPixelCaptureButton = new JButton(HEALTH_BAR_PIXEL_CAPTURE_BUTTON_TEXT);
        healthBarPixelCaptureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetHealthBarPixel();
            }            
        });
        healthBarPixelCaptureButton.setEnabled(false);
        healthBarPixelCaptureButton.setBackground(Color.WHITE);
        screenDetectionPixelCaptureButtonsPanel.add(healthBarPixelCaptureButton);
        
        screenDetectionPixelCaptureDialog = new JDialog(mainFrame, "Capture The Screen Detection Pixels", false);
        screenDetectionPixelCaptureDialog.setContentPane(screenDetectionPixelCapturePanel);
        screenDetectionPixelCaptureDialog.setSize(dialogWidth, dialogHeight);
        screenDetectionPixelCaptureDialog.setLocation((int)windowLocationX + (int)windowWidth, (int)windowLocationY + ((int)windowHeight - dialogHeight));
        screenDetectionPixelCaptureDialog.setVisible(false);
       
        mainFrame.setVisible(true);
    }
    
    public void checkBoxClicked(ItemEvent e) {
        JCheckBox source = (JCheckBox)e.getItemSelectable();
        int selection = e.getStateChange();
        
        if (source == moveForwardCheckBox) {
            if (selection == ItemEvent.SELECTED) {
                holdGuardCheckBox.setEnabled(false);
                holdGuardCheckBox.setSelected(false);
                keepDodgingCheckBox.setEnabled(false);
                keepDodgingCheckBox.setSelected(false);
            } else {
                if (!tapAttackCheckBox.isSelected() && !holdAttackCheckBox.isSelected()) {
                    holdGuardCheckBox.setEnabled(true);
                    keepDodgingCheckBox.setEnabled(true);
                }
            }
        } else if (source == tapAttackCheckBox) {
            if (selection == ItemEvent.SELECTED) {
                holdAttackCheckBox.setSelected(false);
                holdGuardCheckBox.setEnabled(false);
                holdGuardCheckBox.setSelected(false);
                keepDodgingCheckBox.setEnabled(false);
                keepDodgingCheckBox.setSelected(false);
            } else {
                if (!tapAttackCheckBox.isSelected() && !holdAttackCheckBox.isSelected()) {
                    useCombosCheckBox.setSelected(false);
                    useSkillsCheckBox.setSelected(false);
                    if (!moveForwardCheckBox.isSelected()) {
                        holdGuardCheckBox.setEnabled(true);
                        keepDodgingCheckBox.setEnabled(true);
                    }
                }
            }
        } else if (source == holdAttackCheckBox) {
            if (selection == ItemEvent.SELECTED) {
                tapAttackCheckBox.setSelected(false);
                holdGuardCheckBox.setEnabled(false);
                holdGuardCheckBox.setSelected(false);
                keepDodgingCheckBox.setEnabled(false);
                keepDodgingCheckBox.setSelected(false);
            } else {
                if (!tapAttackCheckBox.isSelected() && !holdAttackCheckBox.isSelected()) {
                    useCombosCheckBox.setSelected(false);
                    useSkillsCheckBox.setSelected(false);
                    if (!moveForwardCheckBox.isSelected()) {
                        holdGuardCheckBox.setEnabled(true);
                        keepDodgingCheckBox.setEnabled(true);
                    }
                }
            }
        } else if (source == usePotionCheckBox) {
            resetPotionsCount();
        } else if (source == useLinkAttackCheckBox) {
            // Do nothing
        } else if (source == useSBACheckBox) {
            // Do nothing
        } else if (source == useTargetingCheckBox) {
            // Do nothing
        } else if (source == panCameraCheckBox) {
            // Do nothing
        } else if (source == optimizeforSlimepedeCheckBox) {
            if (selection == ItemEvent.SELECTED) {
                if (!tapAttackCheckBox.isSelected() && !holdAttackCheckBox.isSelected()) {
                    tapAttackCheckBox.setSelected(true);
                }
                moveForwardCheckBox.setEnabled(false);
                moveForwardCheckBox.setSelected(true);
                tapAttackCheckBox.setEnabled(false);
                holdAttackCheckBox.setEnabled(false);
                usePotionCheckBox.setEnabled(false);
                usePotionCheckBox.setSelected(false);
                useLinkAttackCheckBox.setEnabled(false);
                useLinkAttackCheckBox.setSelected(false);
                useSBACheckBox.setEnabled(false);
                useSBACheckBox.setSelected(false);
                useTargetingCheckBox.setEnabled(false);
                useTargetingCheckBox.setSelected(false);
                panCameraCheckBox.setEnabled(false);
                panCameraCheckBox.setSelected(false);
                enableAdvancedOptionsCheckBox.setEnabled(false);
                enableAdvancedOptionsCheckBox.setSelected(false);
            } else {
                moveForwardCheckBox.setEnabled(true);
                tapAttackCheckBox.setEnabled(true);
                holdAttackCheckBox.setEnabled(true);
                usePotionCheckBox.setEnabled(true);
                useLinkAttackCheckBox.setEnabled(true);
                useSBACheckBox.setEnabled(true);
                useTargetingCheckBox.setEnabled(true);
                panCameraCheckBox.setEnabled(true);
                enableAdvancedOptionsCheckBox.setEnabled(true);
            }
        } else if (source == enableAdvancedOptionsCheckBox) {
            if (selection == ItemEvent.SELECTED) {
                useCombosCheckBox.setEnabled(true);
                useCombosCheckBox.setSelected(true);
                useSkillsCheckBox.setEnabled(true);
                useSkillsCheckBox.setSelected(true);
                usePotionCheckBox.setEnabled(true);
                usePotionCheckBox.setSelected(true);
                // Display a dialog to capture the location and the color of a pixel on the screen that can be used to determine the state of the screen.
                displayScreenDetectionPixelCaptureDialog();
            } else {
                useCombosCheckBox.setEnabled(false);
                useCombosCheckBox.setSelected(false);
                useSkillsCheckBox.setEnabled(false);
                useSkillsCheckBox.setSelected(false);
                usePotionCheckBox.setEnabled(false);
                usePotionCheckBox.setSelected(false);
            }
        } else if (source == useCombosCheckBox) {
            if (selection == ItemEvent.SELECTED) {
                if (!tapAttackCheckBox.isSelected() && !holdAttackCheckBox.isSelected()) {
                    tapAttackCheckBox.setSelected(true);
                }
                holdGuardCheckBox.setEnabled(false);
                holdGuardCheckBox.setSelected(false);
                keepDodgingCheckBox.setEnabled(false);
                keepDodgingCheckBox.setSelected(false);
            } else {
                if (!useCombosCheckBox.isSelected() && !useSkillsCheckBox.isSelected() && 
                    !moveForwardCheckBox.isSelected() && !tapAttackCheckBox.isSelected() && !holdAttackCheckBox.isSelected()) {
                    holdGuardCheckBox.setEnabled(true);
                    keepDodgingCheckBox.setEnabled(true);
                }
            }
        } else if (source == useSkillsCheckBox) {
            if (selection == ItemEvent.SELECTED) {
                skillRotation = 0;
                if (!tapAttackCheckBox.isSelected() && !holdAttackCheckBox.isSelected()) {
                    tapAttackCheckBox.setSelected(true);
                }
                holdGuardCheckBox.setEnabled(false);
                holdGuardCheckBox.setSelected(false);
                keepDodgingCheckBox.setEnabled(false);
                keepDodgingCheckBox.setSelected(false);
            } else {
                if (!useCombosCheckBox.isSelected() && !useSkillsCheckBox.isSelected() && 
                    !moveForwardCheckBox.isSelected() && !tapAttackCheckBox.isSelected() && !holdAttackCheckBox.isSelected()) {
                    holdGuardCheckBox.setEnabled(true);
                    keepDodgingCheckBox.setEnabled(true);
                }
            }
        } else if (source == holdGuardCheckBox) {
            if (selection == ItemEvent.SELECTED) {
                moveForwardCheckBox.setEnabled(false);
                moveForwardCheckBox.setSelected(false);
                tapAttackCheckBox.setEnabled(false);
                tapAttackCheckBox.setSelected(false);
                holdAttackCheckBox.setEnabled(false);
                holdAttackCheckBox.setSelected(false);
                useCombosCheckBox.setEnabled(false);
                useCombosCheckBox.setSelected(false);
                useSkillsCheckBox.setEnabled(false);
                useSkillsCheckBox.setSelected(false);
            } else {
                if (!keepDodgingCheckBox.isSelected()) {
                    moveForwardCheckBox.setEnabled(true);
                    tapAttackCheckBox.setEnabled(true);
                    holdAttackCheckBox.setEnabled(true);
                    useCombosCheckBox.setEnabled(true);
                    useSkillsCheckBox.setEnabled(true);
                }
            }
        } else if (source == keepDodgingCheckBox) {
            if (selection == ItemEvent.SELECTED) {
                moveForwardCheckBox.setEnabled(false);
                moveForwardCheckBox.setSelected(false);
                tapAttackCheckBox.setEnabled(false);
                tapAttackCheckBox.setSelected(false);
                holdAttackCheckBox.setEnabled(false);
                holdAttackCheckBox.setSelected(false);
                useCombosCheckBox.setEnabled(false);
                useCombosCheckBox.setSelected(false);
                useSkillsCheckBox.setEnabled(false);
                useSkillsCheckBox.setSelected(false);
            } else {
                if (!holdGuardCheckBox.isSelected()) {
                    moveForwardCheckBox.setEnabled(true);
                    tapAttackCheckBox.setEnabled(true);
                    holdAttackCheckBox.setEnabled(true);
                    useCombosCheckBox.setEnabled(true);
                    useSkillsCheckBox.setEnabled(true);
                }
            }
        }    
    }
    
    public void selectDefaultUIOptions() {
        moveForwardCheckBox.setSelected(true);
        tapAttackCheckBox.setSelected(true);
        useLinkAttackCheckBox.setSelected(true);
        useSBACheckBox.setSelected(true);
        useTargetingCheckBox.setSelected(true);
        enableAdvancedOptionsCheckBox.setSelected(true);
        useCombosCheckBox.setSelected(true);
        useSkillsCheckBox.setSelected(true);
        usePotionCheckBox.setSelected(true);
    }
    
    public void displayScreenDetectionPixelCaptureDialog() {
        int mainFrameLocationX = mainFrame.getLocation().x;
        int mainFrameLocationY = mainFrame.getLocation().y;
        screenDetectionPixelCaptureDialog.setLocation(mainFrameLocationX, mainFrameLocationY - dialogHeight);
        if (!pointerT.isRunning()) {
            pointerT.start();
        }
        
        if (missionResultPixelColor == null) {
            missionResultPixelRecordingOn = true;
            missionResultPixelCaptureButton.setEnabled(false);
            missionResultPixelCaptureButton.setBackground(Color.WHITE);
            missionResultPixelCaptureButton.setText(MISSION_RESULT_PIXEL_CAPTURE_BUTTON_TEXT);
        }
        
        if (healthBarPixelColor == null) {
            healthBarPixelRecordingOn = true;
            healthBarPixelCaptureButton.setEnabled(false);
            healthBarPixelCaptureButton.setBackground(Color.WHITE);
            healthBarPixelCaptureButton.setText(HEALTH_BAR_PIXEL_CAPTURE_BUTTON_TEXT);
        }
        
        screenDetectionPixelCaptureDialog.setVisible(true);
    }
    
    public void updateCurrentScreenPixel() {
        if (MouseInfo.getPointerInfo() == null || MouseInfo.getPointerInfo().getLocation() == null) {return;}
        currentScreenPixelX = MouseInfo.getPointerInfo().getLocation().x;
        currentScreenPixelY = MouseInfo.getPointerInfo().getLocation().y;       
        currentScreenPixelColor = robot.getPixelColor(currentScreenPixelX, currentScreenPixelY);
        currentScreenPixelR = currentScreenPixelColor.getRed();
        currentScreenPixelG = currentScreenPixelColor.getGreen();
        currentScreenPixelB = currentScreenPixelColor.getBlue();
        
        r_foreground = 255 - currentScreenPixelR;
        g_foreground = 255 - currentScreenPixelG;
        b_foreground = 255 - currentScreenPixelB;
        screenDetectionPointerLabelTextColor = new Color(r_foreground, g_foreground, b_foreground);
        
        screenDetectionPointerLabel.setBackground(currentScreenPixelColor);
        screenDetectionPointerLabel.setForeground(screenDetectionPointerLabelTextColor);
        screenDetectionPointerLabel.setText("Cursor Coordinates: " + currentScreenPixelX + ", " + currentScreenPixelY + 
            " | Pixel Color: r" + currentScreenPixelR + ", g" + currentScreenPixelG + ", b" + currentScreenPixelB);
    }
    
    public void recordMissionResultPixel() {
        missionResultPixelX = MouseInfo.getPointerInfo().getLocation().x;
        missionResultPixelY = MouseInfo.getPointerInfo().getLocation().y;       
        missionResultPixelColor = robot.getPixelColor(missionResultPixelX, missionResultPixelY);
        missionResultPixelR = missionResultPixelColor.getRed();
        missionResultPixelG = missionResultPixelColor.getGreen();
        missionResultPixelB = missionResultPixelColor.getBlue();
        
        missionResultPixelRecordingOn = false;
        missionResultPixelCaptureButton.setEnabled(true);
        missionResultPixelCaptureButton.setBackground(Color.CYAN);
        missionResultPixelCaptureButton.setText(MISSION_RESULT_PIXEL_RE_CAPTURE_BUTTON_TEXT + " | x:" + missionResultPixelX + ", y:" + missionResultPixelY + 
            ", r:" + missionResultPixelR + ", g:" + missionResultPixelG + ", b:" + missionResultPixelB);
        if (!missionResultPixelRecordingOn && !healthBarPixelRecordingOn) {
            pointerT.stop();
        }
    }
    
    public void recordHealthBarPixel() {
        healthBarPixelX = MouseInfo.getPointerInfo().getLocation().x;
        healthBarPixelY = MouseInfo.getPointerInfo().getLocation().y;       
        healthBarPixelColor = robot.getPixelColor(healthBarPixelX, healthBarPixelY);
        healthBarPixelR = healthBarPixelColor.getRed();
        healthBarPixelG = healthBarPixelColor.getGreen();
        healthBarPixelB = healthBarPixelColor.getBlue();            
        
        healthBarPixelRecordingOn = false;
        healthBarPixelCaptureButton.setEnabled(true);
        healthBarPixelCaptureButton.setBackground(Color.CYAN);
        healthBarPixelCaptureButton.setText(HEALTH_BAR_PIXEL_RE_CAPTURE_BUTTON_TEXT + " | x:" + healthBarPixelX + ", y:" + healthBarPixelY);
        if (!missionResultPixelRecordingOn && !healthBarPixelRecordingOn) {
            pointerT.stop();
        }
    }
    
    public void resetMissionResultPixel() {
        missionResultPixelColor = null;
        missionResultPixelX = -1;
        missionResultPixelY = -1;
        missionResultPixelR = -1;
        missionResultPixelG = -1;
        missionResultPixelB = -1;
        
        if (!pointerT.isRunning()) {
            pointerT.start();
        }
        missionResultPixelRecordingOn = true;
        missionResultPixelCaptureButton.setEnabled(false);
        missionResultPixelCaptureButton.setBackground(Color.WHITE);
        missionResultPixelCaptureButton.setText(MISSION_RESULT_PIXEL_CAPTURE_BUTTON_TEXT);
    }
    
    public void resetHealthBarPixel() {
        healthBarPixelColor = null;
        healthBarPixelX = -1;
        healthBarPixelY = -1;
        healthBarPixelR = -1;
        healthBarPixelG = -1;
        healthBarPixelB = -1;
        
        if (!pointerT.isRunning()) {
            pointerT.start();
        }
        healthBarPixelRecordingOn = true;
        healthBarPixelCaptureButton.setEnabled(false);
        healthBarPixelCaptureButton.setBackground(Color.WHITE);
        healthBarPixelCaptureButton.setText(HEALTH_BAR_PIXEL_CAPTURE_BUTTON_TEXT);
    }
    
    public boolean isMissionResultPixelOnScreen() {
        if (missionResultPixelRecordingOn || missionResultPixelColor == null || missionResultPixelX == -1 || missionResultPixelR == -1) {
            // screenIndicator.setText("ScreenState: Unknown");
            // screenIndicator.setForeground(Color.GRAY);
        
            return false;
        }
        
        /*mainInstructionTextArea.setText("missionResultPixelX: " + missionResultPixelX + " missionResultPixelY: " + missionResultPixelY + "\n" +
            "missionResultPixelR: " + missionResultPixelR + " missionResultPixelG: " + missionResultPixelG + " missionResultPixelB: " + missionResultPixelB + "\n" + 
            "currentScreenPixelR: " + currentScreenPixelR + " currentScreenPixelG: " + currentScreenPixelG + " currentScreenPixelB: " + currentScreenPixelB);*/
        
        comparisonPixelColor = robot.getPixelColor(missionResultPixelX, missionResultPixelY);
        comparisonPixelR = comparisonPixelColor.getRed();
        comparisonPixelG = comparisonPixelColor.getGreen();
        comparisonPixelB = comparisonPixelColor.getBlue();
        
        diff_r_g_comparison = comparisonPixelR - comparisonPixelG;
        diff_g_b_comparison = comparisonPixelG - comparisonPixelB;
        diff_r_g_missionResult = missionResultPixelR - missionResultPixelG;
        diff_g_b_missionResult = missionResultPixelG - missionResultPixelB;
        
        missionResultPixelComparisonThreshold = 5;        
        if (Math.abs(diff_r_g_comparison - diff_r_g_missionResult) <  missionResultPixelComparisonThreshold && 
            Math.abs(diff_g_b_comparison - diff_g_b_missionResult) <  missionResultPixelComparisonThreshold) {
            // screenIndicator.setText("ScreenState: In Mission Result");
            // screenIndicator.setForeground(Color.BLUE);
        
            return true;
        }
        
        // screenIndicator.setText("ScreenState: In Combat");
        // screenIndicator.setForeground(Color.ORANGE);
        
        return false;
    }
    
    public boolean isMissionResultPopUpOnScreen() {
        if (missionResultPopUpColor == null) {
            return false;
        }
        
        comparisonPixelColor = robot.getPixelColor(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
        comparisonPixelR = comparisonPixelColor.getRed();
        comparisonPixelG = comparisonPixelColor.getGreen();
        comparisonPixelB = comparisonPixelColor.getBlue();
        
        diff_r_g_comparison = comparisonPixelR - comparisonPixelG;
        diff_g_b_comparison = comparisonPixelG - comparisonPixelB;
        diff_r_g_missionResult = missionResultPopUpColor.getRed() - missionResultPopUpColor.getGreen();
        diff_g_b_missionResult = missionResultPopUpColor.getGreen() - missionResultPopUpColor.getBlue();
        
        missionResultPixelComparisonThreshold = 15;
        if (Math.abs(diff_r_g_comparison - diff_r_g_missionResult) <  missionResultPixelComparisonThreshold && 
            Math.abs(diff_g_b_comparison - diff_g_b_missionResult) <  missionResultPixelComparisonThreshold) {
            return true;
        }
        
        return false;
    }
    
    public boolean isInMissionResultScreen() {
        if (isMissionResultPixelOnScreen() || isMissionResultPopUpOnScreen()) {
            // If the health bar pixel is set, use it as a point of reference to make the decision.
            if (healthBarPixelColor != null && healthBarPixelX != -1) {                
                if (isHealthBarPixelGreen() || isHealthBarPixelRed()) {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    public boolean isHealthBarPixelGreen() {
        if (healthBarPixelRecordingOn || healthBarPixelColor == null || healthBarPixelX == -1 || healthBarPixelG == -1) {
            return true;
        }
        
        comparisonPixelColor = robot.getPixelColor(healthBarPixelX, healthBarPixelY);
        comparisonPixelR = comparisonPixelColor.getRed();
        comparisonPixelG = comparisonPixelColor.getGreen();
        comparisonPixelB = comparisonPixelColor.getBlue();
        
        healthBarPixelComparisonThreshold = 5;        
        if (Math.abs(comparisonPixelR - 255) > healthBarPixelComparisonThreshold &&
            Math.abs(comparisonPixelG - 255) < healthBarPixelComparisonThreshold &&
            Math.abs(comparisonPixelB - 255) > healthBarPixelComparisonThreshold) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isHealthBarPixelRed() {
        if (healthBarPixelRecordingOn || healthBarPixelColor == null || healthBarPixelX == -1 || healthBarPixelR == -1) {
            return false;
        }
        
        comparisonPixelColor = robot.getPixelColor(healthBarPixelX, healthBarPixelY);
        comparisonPixelR = comparisonPixelColor.getRed();
        comparisonPixelG = comparisonPixelColor.getGreen();
        comparisonPixelB = comparisonPixelColor.getBlue();
        
        healthBarPixelComparisonThreshold = 5;        
        if (Math.abs(comparisonPixelR - 255) < healthBarPixelComparisonThreshold &&
            Math.abs(comparisonPixelG - 255) > healthBarPixelComparisonThreshold &&
            Math.abs(comparisonPixelB - 255) > healthBarPixelComparisonThreshold) {
            return true;
        } else {
            return false;
        }
    }
    
    public void missionResultScreenActions() {
        // Wait for a pop up to potentially show up before making any inputs.
        robot.delay(3000);        
        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
            resetAll();
            return;
        }
        
        // Releases some of the keys that might have been pressed
        robot.keyRelease(KeyEvent.VK_T);
        robot.keyRelease(KeyEvent.VK_Q);
        robot.mouseRelease(InputEvent.BUTTON2_MASK);
        
        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
            resetAll();
            return;
        }
        
        // Press the "Up" key followed by the "Enter" key twice; used to select "Yes" on the "do you wish to continue repeating" pop up.
        robot.keyPress(KeyEvent.VK_UP);
        robot.keyRelease(KeyEvent.VK_UP);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        
        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
            resetAll();
            return;
        }
        
        // Refreshes the potions count after each mission.
        resetPotionsCount();
    }
    
    public void usePotions() {
        if (usePotionCheckBox.isSelected()) {
            if (isHealthBarPixelRed() && numRevivalPotion > 0) {
                // Press "V" to use the Revival potion.                    
                robot.keyPress(KeyEvent.VK_V);
                robot.delay(300);
                robot.keyRelease(KeyEvent.VK_V);
                
                if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                    resetAll();
                    return;
                }
                
                /* After a small delay, verify whether the potion was successfully consumed (by checking the health bar again).
                 * Deduct the internal potion count if verified to be consumed.
                 * This is not fully deterministic as sometimes other actions may replenish the health and cause the program
                 * to think that a potion was consumed.
                 * Still, battle-performance-wise, it is better to have left over potions than to 
                 * constantly attempt to drink a non-existent potion.*/
                robot.delay(200);
                if (!isHealthBarPixelRed()) {
                    numRevivalPotion--;
                }
                
                if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                    resetAll();
                    return;
                }
            } else {            
                if (numGreenPotion > 0) {
                    if (!isHealthBarPixelGreen()) {
                        // Press "X" to use the Green potion.
                        robot.keyPress(KeyEvent.VK_X);
                        robot.delay(300);
                        robot.keyRelease(KeyEvent.VK_X);
                        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                            resetAll();
                            return;
                        }
                        
                        /* After a small delay, verify whether the potion was successfully consumed (by checking the health bar again).
                         * Deduct the internal potion count if verified to be consumed.
                         * This is not fully deterministic as sometimes other actions may replenish the health and cause the program
                         * to think that a potion was consumed.
                         * Still, battle-performance-wise, it is better to have left over potions than to 
                         * constantly attempt to drink a non-existent potion.*/
                        robot.delay(200);
                        if (isHealthBarPixelGreen()) {
                            numGreenPotion--;
                        }
                        
                        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                            resetAll();
                            return;
                        }
                    } else {
                        return;   
                    }
                }
                
                if (numBluePotion > 0) {
                    if (!isHealthBarPixelGreen()) {
                        // Press "C" to use the Blue potion.
                        robot.keyPress(KeyEvent.VK_C);
                        robot.delay(300);
                        robot.keyRelease(KeyEvent.VK_C);
                        
                        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                            resetAll();
                            return;
                        }
                        
                        /* After a small delay, verify whether the potion was successfully consumed (by checking the health bar again).
                         * Deduct the internal potion count if verified to be consumed.
                         * This is not fully deterministic as sometimes other actions may replenish the health and cause the program
                         * to think that a potion was consumed.
                         * Still, battle-performance-wise, it is better to have left over potions than to 
                         * constantly attempt to drink a non-existent potion.*/
                        robot.delay(200);
                        if (isHealthBarPixelGreen()) {
                            numBluePotion--;
                        }
                        
                        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                            resetAll();
                            return;
                        }
                    } else {
                        return;   
                    }
                }
                
                if (numMegaPotion >  0) {
                    if (!isHealthBarPixelGreen()) {
                        // Press "Z" to use the Mega potion.
                        robot.keyPress(KeyEvent.VK_Z);
                        robot.delay(300);
                        robot.keyRelease(KeyEvent.VK_Z);
                        
                        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                            resetAll();
                            return;
                        }
                        
                        /* After a small delay, verify whether the potion was successfully consumed (by checking the health bar again).
                         * Deduct the internal potion count if verified to be consumed.
                         * This is not fully deterministic as sometimes other actions may replenish the health and cause the program
                         * to think that a potion was consumed.
                         * Still, battle-performance-wise, it is better to have left over potions than to 
                         * constantly attempt to drink a non-existent potion.*/
                        robot.delay(200);
                        if (isHealthBarPixelGreen()) {
                            numMegaPotion--;
                        }
                        
                        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                            resetAll();
                            return;
                        }
                    } else {
                        return;   
                    }
                }
            }
        }
    }
    
    public void holdTargeting() {        
        if (useTargetingCheckBox.isSelected()) {
            // Hold middle click on the mouse; used for toggle targetting.
            robot.mousePress(InputEvent.BUTTON2_MASK);            
        }
        
        // Check whether the user has moved their mouse during the robot actions. If the mouse was moved, terminate the robot and give back control to the user. 
        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
            resetAll();
            return;
        }
    }
    
    public void releaseTargeting() {        
        if (useTargetingCheckBox.isSelected()) {
            // Release middle click on the mouse.
            robot.mouseRelease(InputEvent.BUTTON2_MASK);
        }
        
        // Check whether the user has moved their mouse during the robot actions. If the mouse was moved, terminate the robot and give back control to the user. 
        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
            resetAll();
            return;
        }
    }
    
    public void panCamera() {
        if (panCameraCheckBox.isSelected()) {
            // Move the mouse to the right; used to pan the camera.                
            robot.setAutoDelay(0);
            for (i = 0; i < 12; i++) {
                previousMousePosX += 10;
                previousMousePosY += 0;
                robot.mouseMove(previousMousePosX, previousMousePosY);
                robot.delay(50);
            }
            robot.setAutoDelay(ROBOT_DEFAULT_AUTO_DELAY);
            
        }
        
        // Add a small delay here before setting the new previousMouse position since sometimes the camera keeps panning a little after the mouse movement stops.
        robot.delay(100);
        previousMousePosX = MouseInfo.getPointerInfo().getLocation().x;
        previousMousePosY = MouseInfo.getPointerInfo().getLocation().y;
        
        // Check whether the user has moved their mouse during the robot actions. If the mouse was moved, terminate the robot and give back control to the user. 
        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
            resetAll();
            return;
        }
    }
    
    public void moveForward() {
        // Press and hold the "W" key; used for movement.
        if (moveForwardCheckBox.isSelected()) {
            robot.keyPress(KeyEvent.VK_W);
            if (optimizeforSlimepedeCheckBox.isSelected() && holdAttackCheckBox.isSelected()) {
                robot.delay(50);
            } else if (optimizeforSlimepedeCheckBox.isSelected()) {
                // No delay; takes 1 small step forward.
            } else {
                // Standard move-forward duration.
                robot.delay(1000);
            }
            robot.keyRelease(KeyEvent.VK_W);
        }
        
        // Check whether the user has moved their mouse during the robot actions. If the mouse was moved, terminate the robot and give back control to the user. 
        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
            resetAll();
            return;
        }
    }
    
    public void useLightAttacks() {
        // Left clicks on the mouse; used for attacking and confirming menu items.
        if (tapAttackCheckBox.isSelected()) {
            for (i = 0; i < numLeftClicks; i++) {
                if (isInMissionResultScreen()) {
                    return;
                }
                
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                
                if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                    resetAll();
                    return;
                }
            }
        } else if (holdAttackCheckBox.isSelected()) {
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(500);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            
            if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                resetAll();
                return;
            }
        }
        
        // Add a delay after the left click attacks to take the animation recovery time into consideration.
        if (optimizeforSlimepedeCheckBox.isSelected()) {
            robot.delay(1000);
        } else if (useCombosCheckBox.isSelected()) {
            // No delay if the Combo Finisher needs to connect after the light attacks.
        } else {
            robot.delay(100);
        }
        
        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
            resetAll();
            return;
        }
    }
    
    public void useComboFinishers() {
        if (useCombosCheckBox.isSelected()) {
            // Right clicks on the mouse; used for comboing.
            if (tapAttackCheckBox.isSelected()) {
                for (i = 0; i < numRightClicks; i++) {
                    if (isInMissionResultScreen()) {
                        return;
                    }
                    
                    robot.mousePress(InputEvent.BUTTON3_MASK);
                    robot.mouseRelease(InputEvent.BUTTON3_MASK);
                    
                    if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                        resetAll();
                        return;
                    }
                }
            } else if (holdAttackCheckBox.isSelected()) {
                robot.mousePress(InputEvent.BUTTON3_MASK);
                robot.delay(5000);
                robot.mouseRelease(InputEvent.BUTTON3_MASK);
                
                if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                    resetAll();
                    return;
                }
            }
        
            // Adds delay to complete the Combo Finisher animations (after the right clicks)
            robot.delay(100);
            
            if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                resetAll();
                return;
            }
        }
    }
    
    public void useSkills() {
        // Presses the skill button then presses one of the skills; uses all 4 skills in a rotation.
        if (useSkillsCheckBox.isSelected()) {
            robot.keyPress(KeyEvent.VK_T);
            
            if (tapAttackCheckBox.isSelected()) {
                if (skillRotation == 0) {
                    for (i = 0; i < numSkillClicks; i++) {
                        if (isInMissionResultScreen()) {
                            return;
                        }
                        
                        robot.keyPress(KeyEvent.VK_1);
                        robot.keyRelease(KeyEvent.VK_1);
                        
                        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                            resetAll();
                            return;
                        }
                    }
                    skillRotation = 1;
                } else if (skillRotation == 1) {
                    for (i = 0; i < numSkillClicks; i++) {
                        if (isInMissionResultScreen()) {
                            return;
                        }
                        
                        robot.keyPress(KeyEvent.VK_2);
                        robot.keyRelease(KeyEvent.VK_2);
                        
                        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                            resetAll();
                            return;
                        }
                    }
                    skillRotation = 2;
                } else if (skillRotation == 2) {
                    for (i = 0; i < numSkillClicks; i++) {
                        if (isInMissionResultScreen()) {
                            return;
                        }
                        
                        robot.keyPress(KeyEvent.VK_3);
                        robot.keyRelease(KeyEvent.VK_3);
                        
                        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                            resetAll();
                            return;
                        }
                    }
                    skillRotation = 3;
                } else if (skillRotation == 3) {    
                    for (i = 0; i < numSkillClicks; i++) {
                        if (isInMissionResultScreen()) {
                            return;
                        }
                        
                        robot.keyPress(KeyEvent.VK_4);
                        robot.keyRelease(KeyEvent.VK_4);
                        
                        if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                            resetAll();
                            return;
                        }
                    }
                    skillRotation = 0;
                }
            } else if (holdAttackCheckBox.isSelected()) {
                if (skillRotation == 0) {
                    robot.keyPress(KeyEvent.VK_1);
                    robot.delay(5000);
                    robot.keyRelease(KeyEvent.VK_1);
                    
                    if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                        resetAll();
                        return;
                    }
                    skillRotation = 1;
                } else if (skillRotation == 1) {
                    robot.keyPress(KeyEvent.VK_2);
                    robot.delay(5000);
                    robot.keyRelease(KeyEvent.VK_2);
                    
                    if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                        resetAll();
                        return;
                    }
                    skillRotation = 2;
                } else if (skillRotation == 2) {
                    robot.keyPress(KeyEvent.VK_3);
                    robot.delay(5000);
                    robot.keyRelease(KeyEvent.VK_3);
                    
                    if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                        resetAll();
                        return;
                    }
                    skillRotation = 3;
                } else if (skillRotation == 3) {
                    robot.keyPress(KeyEvent.VK_4);
                    robot.delay(5000);
                    robot.keyRelease(KeyEvent.VK_4);
                    
                    if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                        resetAll();
                        return;
                    }
                    skillRotation = 0;
                }
            }
            
            robot.keyRelease(KeyEvent.VK_T);
            
            if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                resetAll();
                return;
            }
            
            // Add delay to account for Skill animations.
            robot.delay(500);
            
            if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                resetAll();
                return;
            }
        }
    }
    
    public void holdGuard() {
        // Press and hold the "Q" key to keep guarding. This key is not released until combat ends or when everything resets.
        if (holdGuardCheckBox.isSelected()) {
            robot.keyPress(KeyEvent.VK_Q);
            
            if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                resetAll();
                return;
            }
        }
    }
    
    public void useDodge() {
        // Press the "E" key to dodge.
        if (keepDodgingCheckBox.isSelected()) {
            robot.keyPress(KeyEvent.VK_E);
            robot.keyRelease(KeyEvent.VK_E);
            
            if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                resetAll();
                return;
            }
        }
    }
    
    public void useLinkAttack() {
        if (useLinkAttackCheckBox.isSelected()) {
            robot.keyPress(KeyEvent.VK_R);
            robot.keyRelease(KeyEvent.VK_R);
            
            if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                resetAll();
                return;
            }
        }
    }
    
    public void useSBA() {
        if (useSBACheckBox.isSelected()) {
            robot.keyPress(KeyEvent.VK_G);
            robot.keyRelease(KeyEvent.VK_G);
            
            if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                resetAll();
                return;
            }
        }
    }
    
    public void recoverFromCritical() {
        if (usePotionCheckBox.isSelected() && numRevivalPotion > 0) {
            usePotions();
        } else {
            // Spams the space bar to recover from "critical" faster.
            robot.setAutoDelay(50);
            for (i = 0; i < 5; i++) {
                robot.keyPress(KeyEvent.VK_SPACE);
                robot.keyRelease(KeyEvent.VK_SPACE);
                
                if (hasMouseMoved(previousMousePosX, previousMousePosY)) {
                    resetAll();
                    return;
                }
            }
            robot.setAutoDelay(ROBOT_DEFAULT_AUTO_DELAY);
        }
    }
    
    public void resetPotionsCount() {
        numGreenPotion = 8;
        numBluePotion = 6;
        numMegaPotion = 6;
        numRevivalPotion = 3;
    }

    public void actionTimerAction() {
        // The AfkFarm action
        if (functionSelector == AutoPilotFunctions.AFK_FARM) {
            // Record mouse position before the robot actions.
            previousMousePosX = MouseInfo.getPointerInfo().getLocation().x;
            previousMousePosY = MouseInfo.getPointerInfo().getLocation().y;
            
            if (isInMissionResultScreen()) {
                missionResultScreenActions();
                return;
            }
            
            // Discards the rest of the action script if the main character is in the "critical" state.
            if (healthBarPixelColor != null && healthBarPixelX != -1 && isHealthBarPixelRed()) {
                recoverFromCritical();
                return;
            }
            
            holdTargeting();
            panCamera();
            
            if (isInMissionResultScreen()) {
                missionResultScreenActions();
                return;
            }
            
            moveForward();            
            usePotions();
            
            // Sets the number of left and right clicks to perform, based on whether the user wants to do combos.
            if (useCombosCheckBox.isSelected()) {
                numLeftClicks = 4;
                numRightClicks = 12;
            } else {
                numLeftClicks = 1;
                numRightClicks = 0;
            }
            
            if (isInMissionResultScreen()) {
                missionResultScreenActions();
                return;
            }
            
            useLightAttacks();
            
            useLinkAttack();
            useSBA();
            
            if (isInMissionResultScreen()) {
                missionResultScreenActions();
                return;
            }
            
            useComboFinishers();
            
            useLinkAttack();
            useSBA();            
            usePotions();
            
            if (isInMissionResultScreen()) {
                missionResultScreenActions();
                return;
            }
            
            numSkillClicks = 2;
            useSkills();
            
            useLinkAttack();
            useSBA();
            usePotions();
            
            holdGuard();
            useDodge();
            releaseTargeting();            
        }
    }
    
    public void afkFarmButtonPressed() {
        functionSelector = AutoPilotFunctions.AFK_FARM; // Selects the AfkFarm action
    
        actionT.setDelay(100);
        if (!actionT.isRunning()) {
            actionT.start();
        }
        afkFarmButton.setText(AFK_FARM_RUNNING_BUTTON_TEXT);
        afkFarmButton.setBackground(Color.ORANGE);
    
    }
    
    public boolean hasMouseMoved(int expectedX, int expectedY) {
        mouseMovedThreshold = 10;
        
        if (Math.abs(MouseInfo.getPointerInfo().getLocation().x - expectedX) > mouseMovedThreshold || 
            Math.abs(MouseInfo.getPointerInfo().getLocation().y - expectedY) > mouseMovedThreshold) {
            return true;
        }
        return false;
    }
    
    public void resetAll() {
        // Releases some of the keys that might have been pressed while the resetAll was called.
        robot.setAutoDelay(0);
        robot.keyRelease(KeyEvent.VK_T);
        robot.keyRelease(KeyEvent.VK_Q);
        robot.mouseRelease(InputEvent.BUTTON2_MASK);
        robot.setAutoDelay(ROBOT_DEFAULT_AUTO_DELAY);
        
        // Resets global variables.
        i = 0;        
        functionSelector = AutoPilotFunctions.NOT_SET;
        previousMousePosX = -1;
        previousMousePosY = -1;
        skillRotation = 0;
        resetPotionsCount();
        
        // Stops the Action Timer.
        actionT.stop();
        
        // Resets UI elements.
        afkFarmButton.setText(START_AFK_FARM_BUTTON_TEXT);
        afkFarmButton.setBackground(Color.GREEN);
        // screenIndicator.setText("ScreenState: Unknown");
        // screenIndicator.setForeground(Color.GRAY);
    }
    
    public static void main (String args[]) {
        GBFR_Auto_Pilot mainWindow = new GBFR_Auto_Pilot();
    }
}
