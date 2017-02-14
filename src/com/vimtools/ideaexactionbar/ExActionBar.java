package com.vimtools.ideaexactionbar;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ApplicationComponent;

/**
 * @author hari rangarajan
 */
public class ExActionBar implements ApplicationComponent {
    public ExActionBar() {
    }

    @Override
    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "ExActionBar";
    }
}
