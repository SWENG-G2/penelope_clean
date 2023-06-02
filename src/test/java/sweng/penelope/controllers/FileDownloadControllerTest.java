package sweng.penelope.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import sweng.penelope.services.StorageService;

@ExtendWith(MockitoExtension.class)
public class FileDownloadControllerTest {

    private final DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();

    @InjectMocks
    private FileDownloadController fileDownloadController;

    @Mock
    private StorageService storageServiceMock;

    @Test
    public void serveBirdXMLTest() {
        Long birdId = 1L;
        Resource resourceMock = Mockito.mock(Resource.class);

        when(storageServiceMock.loadAsResourceFromDB(eq("bird"), eq(birdId), anyString())).thenReturn(resourceMock);

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setMethod("GET");

        ResponseEntity<Resource> response = fileDownloadController.serveBirdXML(birdId, mockHttpServletRequest);

        verify(storageServiceMock).loadAsResourceFromDB(eq("bird"), eq(birdId), anyString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resourceMock, response.getBody());
    }

    @Test
    public void serveCampusXMLTest() {
        Long campusId = 1L;
        Resource resourceMock = Mockito.mock(Resource.class);

        when(storageServiceMock.loadAsResourceFromDB("campus", campusId, null)).thenReturn(resourceMock);

        ResponseEntity<Resource> response = fileDownloadController.serveCampusXML(campusId);

        verify(storageServiceMock).loadAsResourceFromDB("campus", campusId, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resourceMock, response.getBody());
    }

    @Test
    public void serveCampusesListXMLTest() {
        Resource resourceMock = Mockito.mock(Resource.class);

        when(storageServiceMock.loadAsResourceFromDB("campusList", null, null)).thenReturn(resourceMock);

        ResponseEntity<Resource> response = fileDownloadController.serveCampusesListXML();

        verify(storageServiceMock).loadAsResourceFromDB("campusList", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resourceMock, response.getBody());
    }

    @Test
    public void serveAssetSuccessTest() throws URISyntaxException, IOException {
        String type = "image";
        String campusId = "1";
        String fileName = "classpath:duckTest.png";
        Resource resourceMock = Mockito.mock(Resource.class);
        URI uri = defaultResourceLoader.getResource(fileName).getFile().toURI();

        when(storageServiceMock.loadAsResource(type, campusId, fileName)).thenReturn(resourceMock);
        when(resourceMock.getURI()).thenReturn(uri);

        ResponseEntity<Resource> response = fileDownloadController.serveAsset(type, campusId, fileName);

        verify(storageServiceMock).loadAsResource(type, campusId, fileName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resourceMock, response.getBody());
        assertEquals(MediaType.valueOf("image/png"), response.getHeaders().getContentType());
    }

    @Test
    public void serveAssetFailTest() throws URISyntaxException, IOException {
        String type = "image";
        String campusId = "1";
        String fileName = "classpath:duckTest.png";
        Resource resourceMock = Mockito.mock(Resource.class);
        URI uri = defaultResourceLoader.getResource(fileName).getFile().toURI();

        when(storageServiceMock.loadAsResource(type, campusId, fileName)).thenReturn(resourceMock);
        when(resourceMock.getURI()).thenReturn(uri);

        ResponseEntity<Resource> response = fileDownloadController.serveAsset(type, campusId, fileName);

        verify(storageServiceMock).loadAsResource(type, campusId, fileName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resourceMock, response.getBody());

        // Wrong content type on purpose, should be image/png
        assertThrows(AssertionError.class, () -> {
            assertEquals(MediaType.valueOf("video/mp4"), response.getHeaders().getContentType());
        }, "Wrong content type");
    }
}
