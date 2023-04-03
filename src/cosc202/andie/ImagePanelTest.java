package cosc202.andie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cosc202.andie.ImagePanel;

public class ImagePanelTest {
    @Test
    void initialDummyTest() {
    }

    @Test
    void getZoomInitialValue(){
         ImagePanel testPanel = new ImagePanel();
        Assertions.assertEquals(100.0, testPanel.getZoom());

    }

    @Test
    void getZoomAftersetZoom(){
        ImagePanel testPanel = new ImagePanel();
        
        // Act
        testPanel.setZoom(0);
        
        Assertions.assertFalse(testPanel.getZoom() == 100.0);
        Assertions.assertTrue(testPanel.getZoom() >= 50.0);
    }


}
