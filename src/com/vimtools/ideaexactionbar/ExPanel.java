package com.vimtools.ideaexactionbar;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.TextFieldWithAutoCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is used to enter ex commands such as searches and "colon" commands
 */
public class ExPanel extends JPanel {

    private DataContext context;

    private Collection<AnAction> getActions() {
        final ActionManager actionManager = ActionManager.getInstance();
        final Collection<String> actionNames = Arrays.asList(actionManager.getActionIds(""));

        List<AnAction> anActionList = actionNames.stream().map(actionManager::getAction).collect(Collectors.toList());
        return anActionList;
    }

    public static ExPanel getInstance(Project project) {
        if (instance == null) {
            instance = new ExPanel(project);
        }

        return instance;
    }

    private ExPanel(Project project) {
        setBorder(BorderFactory.createEtchedBorder());
        label = new JLabel(" ");
        //entry = TextFieldWithAutoCompletion.create(project, getActions(), true, "test");
        AbstractAction action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                ActionManager actionManager = ActionManager.getInstance();
                AnAction action = actionManager.getAction(entry.getText());
                final Application application = ApplicationManager.getApplication();
                application.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        executeAction(action, context, entry.getText());
                        ExPanel.getInstance(project).deactivate(true);
                    }

                });
            }
        };

        InsertHandler<LookupElement> insertHandler = new InsertHandler<LookupElement>() {
            @Override
            public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
                ActionManager actionManager = ActionManager.getInstance();
                AnAction action = actionManager.getAction(entry.getText());
                final Application application = ApplicationManager.getApplication();
                application.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ExPanel.getInstance(project).deactivate(true);
                        executeAction(action, context, entry.getText());
                    }

                });
            }
        };

        CustomAutoCompletionProvider<AnAction> textFieldWithAutoCompletionListProvider = new CustomAutoCompletionProvider<AnAction>(getActions(), insertHandler) {
            @NotNull
            @Override
            protected String getLookupString(@NotNull AnAction anAction) {
                return ActionManager.getInstance().getId(anAction);
            }
            @Nullable
            @Override
            protected String getTailText(@NotNull AnAction anAction) {
                return anAction.getTemplatePresentation().getDescription();
            }

            @Nullable
            @Override
            protected String getTypeText(@NotNull AnAction anAction) {
                return anAction.getTemplatePresentation().getText();
            }

            @Nullable
            @Override
            protected Icon getIcon(@NotNull AnAction anAction) {
                return anAction.getTemplatePresentation().getIcon();
            }


            @Nullable
            @Override
            public PrefixMatcher createPrefixMatcher(@NotNull String s) {
                return new CamelHumpMatcher(s);
            }
        };
        entry = new TextFieldWithAutoCompletion(project, textFieldWithAutoCompletionListProvider, true, "test");

        entry.setBorder(null);

        setFontForElements();

        setForeground(entry.getForeground());
        setBackground(entry.getBackground());

        label.setForeground(entry.getForeground());
        label.setBackground(entry.getBackground());

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        setLayout(layout);
        gbc.gridx = 0;
        layout.setConstraints(this.label, gbc);
        add(this.label);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        layout.setConstraints(entry, gbc);
        add(entry);
        setBorder(BorderFactory.createEtchedBorder());

        adapter = new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                positionPanel();
            }
        };
        getInputMap(
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        ).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeExPanel");


        getInputMap(
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        ).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "execCommand");

        getActionMap().put("closeExPanel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                ExPanel.getInstance(project).deactivate(true);
            }
        });

        getActionMap().put("execCommand", action);
       // setFocusable(true);

    }


    private void setFontForElements() {
//        final Font font = UiHelper.getEditorFont();
//        label.setFont(font);
//        entry.setFont(font);
    }

    /**
     * Turns on the ex entry field for the given editor
     *
     * @param editor   The editor to use for display
     * @param context  The data context
     * @param label    The label for the ex entry (i.e. :, /, or ?)
     * @param initText The initial text for the entry
     * @param count    A holder for the ex entry count
     */
    public void activate(@NotNull Editor editor, DataContext context, @NotNull String label, String initText, int count) {
        //entry.setEditor(editor, context);
        this.label.setText(label);
        this.count = count;
        this.context = context;
        setFontForElements();
        //entry.setDocument(entry.createDefaultModel());
        entry.setText(initText);
        //entry.setType(label);
        parent = editor.getContentComponent();
        if (!ApplicationManager.getApplication().isUnitTestMode()) {
            JRootPane root = SwingUtilities.getRootPane(parent);
            oldGlass = (JComponent)root.getGlassPane();
            oldLayout = oldGlass.getLayout();
            wasOpaque = oldGlass.isOpaque();
            oldGlass.setLayout(null);
            oldGlass.setOpaque(false);
            oldGlass.add(this);
            oldGlass.addComponentListener(adapter);
            positionPanel();
            oldGlass.setVisible(true);
            entry.requestFocusInWindow();
        }
        active = true;
    }

    /**
     * Gets the label for the ex entry. This should be one of ":", "/", or "?"
     *
     * @return The ex entry label
     */
    public String getLabel() {
        return label.getText();
    }

    /**
     * Gets the count given during activation
     *
     * @return The count
     */
    public int getCount() {
        return count;
    }

    /**
     * Pass the keystroke on to the text edit for handling
     *
     * @param stroke The keystroke
     */
    public void handleKey(@NotNull KeyStroke stroke) {
        //entry.handleKey(stroke);
    }

    private void positionPanel() {
        if (parent == null) return;

        Container scroll = SwingUtilities.getAncestorOfClass(JScrollPane.class, parent);
        int height = (int)getPreferredSize().getHeight();
        if (scroll != null) {
            Rectangle bounds = scroll.getBounds();
            bounds.translate(0, scroll.getHeight() - height);
            bounds.height = height;
            Point pos = SwingUtilities.convertPoint(scroll.getParent(), bounds.getLocation(), oldGlass);
            bounds.setLocation(pos);
            setBounds(bounds);
            repaint();
        }
    }

    /**
     * Gets the text entered by the user. This includes any initial text but does not include the label
     *
     * @return The user entered text
     */
    public String getText() {
        return entry.getText();
    }

    @NotNull
    public EditorTextField getEntry() {
        return entry;
    }

    /**
     * Turns off the ex entry field and optionally puts the focus back to the original component
     */
    public void deactivate(boolean refocusOwningEditor) {
        logger.info("deactivate");
        if(this.active) {
            this.active = false;
            if(!ApplicationManager.getApplication().isUnitTestMode()) {
                if(refocusOwningEditor && this.parent != null) {
                    UiHelper.requestFocus(this.parent);
                }

                this.oldGlass.removeComponentListener(this.adapter);
                this.oldGlass.setVisible(false);
                this.oldGlass.remove(this);
                this.oldGlass.setOpaque(this.wasOpaque);
                this.oldGlass.setLayout(this.oldLayout);
            }

            this.parent = null;
        }
    }


    /**
     * Checks if the ex entry panel is currently active
     *
     * @return true if active, false if not
     */
    public boolean isActive() {
        return active;
    }

    @Nullable private JComponent parent;
    @NotNull private final JLabel label;
    @NotNull private final EditorTextField entry;
    private JComponent oldGlass;
    private LayoutManager oldLayout;
    private boolean wasOpaque;
    @NotNull private final ComponentAdapter adapter;
    private int count;
    @Nullable private RangeHighlighter incHighlighter = null;
    private int verticalOffset;
    private int horizontalOffset;

    private void executeAction(@NotNull AnAction action, @NotNull DataContext context, @NotNull String actionName) {
        try {
            executeAction(action, context);
        }
        catch (RuntimeException e) {
            // TODO: Find out if any runtime exceptions may happen here
            assert false : "Error while executing :action " + actionName + " (" + action + "): " + e;
        }
    }

    public static boolean executeAction(@NotNull AnAction action, @NotNull DataContext context) {
        // Hopefully all the arguments are sufficient. So far they all seem to work OK.
        // We don't have a specific InputEvent so that is null
        // What is "place"? Leave it the empty string for now.
        // Is the template presentation sufficient?
        // What are the modifiers? Is zero OK?
        final AnActionEvent event = new AnActionEvent(null, context, "", action.getTemplatePresentation(),
            ActionManager.getInstance(), 0);
        action.update(event);
        if (event.getPresentation().isEnabled()) {
            action.actionPerformed(event);
            return true;
        }
        return false;
    }

    private boolean active;

    private static ExPanel instance;

    private static final Logger logger = Logger.getInstance(ExPanel.class.getName());
}

