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

        when(storageServiceMock.loadAsResourceFromDB(eq("campus"), eq(campusId), anyString())).thenReturn(resourceMock);

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setMethod("GET");

        ResponseEntity<Resource> response = fileDownloadController.serveCampusXML(campusId, mockHttpServletRequest);

        verify(storageServiceMock).loadAsResourceFromDB(eq("campus"), eq(campusId), anyString());

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
    public void nullResourceServesNotFoundAsset() throws URISyntaxException, IOException {
        String type = "image";
        String campusId = "1";
        String fileName = "classpath:duckTest.png";

        when(storageServiceMock.loadAsResource(type, campusId, fileName)).thenReturn(null);

        ResponseEntity<Resource> response = fileDownloadController.serveAsset(type, campusId, fileName);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void badResoruceServesInternalServerErrorAsset() throws URISyntaxException, IOException {
        String type = "image";
        String campusId = "1";
        String fileName = "classpath:duckTest.png";
        Resource resourceMock = Mockito.mock(Resource.class);
        // Bad uri causes mime type to be null
        URI uri = URI.create("file:///the/batcave");

        when(storageServiceMock.loadAsResource(type, campusId, fileName)).thenReturn(resourceMock);
        when(resourceMock.getURI()).thenReturn(uri);

        ResponseEntity<Resource> response = fileDownloadController.serveAsset(type, campusId, fileName);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void nullResourceServersNotFoundXML() {
        // Return null XML, type is arbitrary as same code is executed for all
        when(storageServiceMock.loadAsResourceFromDB("campusList", null, null)).thenReturn(null);

        ResponseEntity<Resource> response = fileDownloadController.serveCampusesListXML();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
