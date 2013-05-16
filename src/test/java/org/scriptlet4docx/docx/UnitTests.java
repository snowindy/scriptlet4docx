package org.scriptlet4docx.docx;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DocxTemplaterTest.class, PlaceholderTest.class, DividedScriptWrapsProcessorTest.class,
        TableScriptingProcessorTest.class, TableScriptingRowProcessorTest.class, TemplateFileManagerTest.class })
public class UnitTests {

}
