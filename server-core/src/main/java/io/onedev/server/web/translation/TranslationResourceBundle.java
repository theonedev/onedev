package io.onedev.server.web.translation;

import java.util.ListResourceBundle;

public abstract class TranslationResourceBundle extends ListResourceBundle {

    @Override
    protected final Object[][] getContents() {
        Object[][] autoContents = getAutoContents();
        Object[][] manualContents = getManualContents();
        
        Object[][] mergedContents = new Object[autoContents.length + manualContents.length][];
        
        System.arraycopy(autoContents, 0, mergedContents, 0, autoContents.length);
        System.arraycopy(manualContents, 0, mergedContents, autoContents.length, manualContents.length);
        
        return mergedContents;
    }

    protected abstract Object[][] getAutoContents();

    protected abstract Object[][] getManualContents();

}
