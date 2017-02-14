package com.vimtools.ideaexactionbar;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.project.Project;

/**
 * @author  hari Rangarajan
 */
public class ExTypedActionHandler implements TypedActionHandler {
  @Override
  public void execute(@NotNull Editor editor, char c, @NotNull DataContext dataContext) {
    final Document document = editor.getDocument();
    Project project = editor.getProject();
    ExPanel panel = ExPanel.getInstance(project);
    if (c == 27) {
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          panel.deactivate(true);
        }
      };
      WriteCommandAction.runWriteCommandAction(project, runnable);
    }
  }
}
