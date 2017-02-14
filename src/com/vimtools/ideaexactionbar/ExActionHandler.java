package com.vimtools.ideaexactionbar;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;

/**
 * @author hari Rangarajan
 */
public class ExActionHandler extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        ExPanel panel = ExPanel.getInstance(e.getProject());

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        panel.activate(editor, e.getDataContext(), ":", "", 1);
    }
}
