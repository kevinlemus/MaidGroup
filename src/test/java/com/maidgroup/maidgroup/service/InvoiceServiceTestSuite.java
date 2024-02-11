package com.maidgroup.maidgroup.service;

import com.maidgroup.maidgroup.dao.InvoiceRepository;
import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.Invoice;
import com.maidgroup.maidgroup.model.User;
import com.maidgroup.maidgroup.model.invoiceinfo.InvoiceItem;
import com.maidgroup.maidgroup.model.invoiceinfo.PaymentStatus;
import com.maidgroup.maidgroup.model.userinfo.Role;
import com.maidgroup.maidgroup.service.exceptions.*;
import com.maidgroup.maidgroup.service.impl.InvoiceServiceImpl;
import com.maidgroup.maidgroup.util.payment.PaymentLinkGenerator;
import com.maidgroup.maidgroup.util.square.WebhookSignatureVerifier;
import com.maidgroup.maidgroup.util.square.mock.SquareClientWrapper;
import com.squareup.square.api.OrdersApi;
import com.squareup.square.exceptions.ApiException;
import com.squareup.square.models.BatchRetrieveOrdersRequest;
import com.squareup.square.models.BatchRetrieveOrdersResponse;
import com.squareup.square.models.Order;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class InvoiceServiceTestSuite {

    @InjectMocks
    private InvoiceServiceImpl sut;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrdersApi ordersApi;

    @Mock
    private WebhookSignatureVerifier webhookSignatureVerifier;

    @Mock
    private SquareClientWrapper squareClientWrapper;

    @Mock
    private EmailService emailService;

    @Mock
    private PaymentLinkGenerator paymentLinkGenerator;


    @Before
    public void testPrep() {
        MockitoAnnotations.initMocks(this);
        when(squareClientWrapper.getOrdersApi()).thenReturn(ordersApi);
    }

    @Test
    public void test_validateInvoice_doesNotThrowException_givenValidInvoice() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setFirstName("Test");
        invoice.setLastName("User");
        invoice.setClientEmail("testuser@example.com");
        invoice.setStreet("123 Test St");
        invoice.setCity("Test City");
        invoice.setState("TS");
        invoice.setZipcode(12345);
        invoice.setDate(LocalDate.now());
        invoice.setItems(Arrays.asList(new InvoiceItem()));

        // Act and Assert
        assertDoesNotThrow(() -> sut.validateInvoice(invoice));
    }

    @Test(expected = InvalidNameException.class)
    public void test_validateInvoice_throwsInvalidNameException_givenEmptyFirstName() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setFirstName("");
        invoice.setLastName("User");
        invoice.setClientEmail("testuser@example.com");
        invoice.setStreet("123 Test St");
        invoice.setCity("Test City");
        invoice.setState("TS");
        invoice.setZipcode(12345);
        invoice.setDate(LocalDate.now());
        invoice.setItems(Arrays.asList(new InvoiceItem()));

        // Act
        sut.validateInvoice(invoice);
    }

// Add similar tests for other fields and exceptions...

    @Test
    public void test_create_createsInvoiceAndReturnsPaymentLink_givenValidInvoiceAndIdempotencyKey() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setFirstName("Test");
        invoice.setLastName("User");
        invoice.setClientEmail("testuser@example.com");
        invoice.setStreet("123 Test St");
        invoice.setCity("Test City");
        invoice.setState("TS");
        invoice.setZipcode(12345);
        invoice.setDate(LocalDate.now());
        invoice.setItems(Arrays.asList(new InvoiceItem()));

        String idempotencyKey = "idempotencyKey";
        String paymentLink = "paymentLink";

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(paymentLinkGenerator.generatePaymentLink(any(Invoice.class), anyString(), any(Order.class))).thenReturn(paymentLink);
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        String createdPaymentLink = sut.create(invoice, idempotencyKey);

        // Assert
        assertNotNull(createdPaymentLink);
        assertEquals(paymentLink, createdPaymentLink);
    }

    @Test(expected = InvalidNameException.class)
    public void test_create_throwsInvalidNameException_whenFirstNameIsNull() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setFirstName(null);  // This should cause validateInvoice to throw an InvalidNameException
        invoice.setLastName("User");
        invoice.setClientEmail("testuser@example.com");
        invoice.setStreet("123 Test St");
        invoice.setCity("Test City");
        invoice.setState("TS");
        invoice.setZipcode(12345);
        invoice.setDate(LocalDate.now());
        invoice.setItems(Arrays.asList(new InvoiceItem()));

        String idempotencyKey = "idempotencyKey";

        // Act
        sut.create(invoice, idempotencyKey);
    }

    @Test(expected = InvalidNameException.class)
    public void test_create_throwsInvalidNameException_whenLastNameIsNull() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setFirstName("Test");
        invoice.setLastName(null);  // This should cause validateInvoice to throw an InvalidNameException
        invoice.setClientEmail("testuser@example.com");
        invoice.setStreet("123 Test St");
        invoice.setCity("Test City");
        invoice.setState("TS");
        invoice.setZipcode(12345);
        invoice.setDate(LocalDate.now());
        invoice.setItems(Arrays.asList(new InvoiceItem()));

        String idempotencyKey = "idempotencyKey";

        // Act
        sut.create(invoice, idempotencyKey);
    }

    @Test(expected = InvalidEmailException.class)
    public void test_create_throwsInvalidEmailException_whenEmailIsNull() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setFirstName("Test");
        invoice.setLastName("User");
        invoice.setClientEmail(null);  // This should cause validateInvoice to throw an InvalidEmailException
        invoice.setStreet("123 Test St");
        invoice.setCity("Test City");
        invoice.setState("TS");
        invoice.setZipcode(12345);
        invoice.setDate(LocalDate.now());
        invoice.setItems(Arrays.asList(new InvoiceItem()));

        String idempotencyKey = "idempotencyKey";

        // Act
        sut.create(invoice, idempotencyKey);
    }

    @Test(expected = InvalidInvoiceException.class)
    public void test_create_throwsInvalidInvoiceException_whenStreetIsNull() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setFirstName("Test");
        invoice.setLastName("User");
        invoice.setClientEmail("testuser@example.com");
        invoice.setStreet(null);  // This should cause validateInvoice to throw an InvalidInvoiceException
        invoice.setCity("Test City");
        invoice.setState("TS");
        invoice.setZipcode(12345);
        invoice.setDate(LocalDate.now());
        invoice.setItems(Arrays.asList(new InvoiceItem()));

        String idempotencyKey = "idempotencyKey";

        // Act
        sut.create(invoice, idempotencyKey);
    }

    @Test(expected = InvalidInvoiceException.class)
    public void test_create_throwsInvalidInvoiceException_whenCityIsNull() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setFirstName("Test");
        invoice.setLastName("User");
        invoice.setClientEmail("testuser@example.com");
        invoice.setStreet("123 Test St");
        invoice.setCity(null);  // This should cause validateInvoice to throw an InvalidInvoiceException
        invoice.setState("TS");
        invoice.setZipcode(12345);
        invoice.setDate(LocalDate.now());
        invoice.setItems(Arrays.asList(new InvoiceItem()));

        String idempotencyKey = "idempotencyKey";

        // Act
        sut.create(invoice, idempotencyKey);
    }

    @Test
    public void test_completePayment_updatesInvoiceStatusToPaid_whenPaymentStatusIsCompleted() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setSent(false);
        invoice.setClientEmail("testuser@example.com");

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        sut.completePayment(invoice, "COMPLETED");

        // Assert
        assertEquals(PaymentStatus.PAID, invoice.getStatus());
        assertTrue(invoice.isSent());
    }

    @Test
    public void test_completePayment_updatesInvoiceStatusToFailed_whenPaymentStatusIsFailed() {
        // Arrange
        Invoice invoice = new Invoice();

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        // Act
        sut.completePayment(invoice, "FAILED");

        // Assert
        assertEquals(PaymentStatus.FAILED, invoice.getStatus());
    }

    @Test
    public void test_delete_deletesInvoice_givenValidIdAndAdminUser() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setId(1L);

        when(invoiceRepository.findById(any(Long.class))).thenReturn(Optional.of(invoice));

        // Act
        sut.delete(1L, requester);

        // Assert
        verify(invoiceRepository, times(1)).delete(invoice);
    }

    @Test(expected = UnauthorizedException.class)
    public void test_delete_throwsUnauthorizedException_givenNonAdminUser() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.USER);

        Invoice invoice = new Invoice();
        invoice.setId(1L);

        when(invoiceRepository.findById(any(Long.class))).thenReturn(Optional.of(invoice));

        // Act
        sut.delete(1L, requester);
    }

    @Test(expected = InvoiceNotFoundException.class)
    public void test_delete_throwsInvoiceNotFoundException_givenInvalidId() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        when(invoiceRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act
        sut.delete(1L, requester);
    }

    @Test
    public void test_getInvoices_returnsAllInvoices_givenAdminUser() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        List<Invoice> expectedInvoices = Arrays.asList(new Invoice(), new Invoice());

        when(invoiceRepository.findAll()).thenReturn(expectedInvoices);

        // Act
        List<Invoice> actualInvoices = sut.getInvoices(requester, null, null, null);

        // Assert
        assertEquals(expectedInvoices, actualInvoices);
    }

    @Test(expected = InvoiceNotFoundException.class)
    public void test_getInvoices_throwsInvoiceNotFoundException_givenNoInvoices() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        when(invoiceRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        sut.getInvoices(requester, null, null, null);
    }

    @Test
    public void test_getInvoiceById_returnsInvoice_givenValidIdAndAdminUser() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        Invoice expectedInvoice = new Invoice();
        expectedInvoice.setId(1L);
        expectedInvoice.setUser(requester); // Add this line

        when(invoiceRepository.findById(any(Long.class))).thenReturn(Optional.of(expectedInvoice));

        // Act
        Invoice actualInvoice = sut.getInvoiceById(1L, requester);

        // Assert
        assertEquals(expectedInvoice, actualInvoice);
    }

    @Test
    public void test_getInvoiceById_returnsInvoice_givenValidIdAndOwnerUser() {
        // Arrange
        User owner = new User();
        owner.setUserId(1L);
        owner.setRole(Role.USER);

        Invoice expectedInvoice = new Invoice();
        expectedInvoice.setId(1L);
        expectedInvoice.setUser(owner);

        when(invoiceRepository.findById(any(Long.class))).thenReturn(Optional.of(expectedInvoice));

        // Act
        Invoice actualInvoice = sut.getInvoiceById(1L, owner);

        // Assert
        assertEquals(expectedInvoice, actualInvoice);
    }


    @Test(expected = UnauthorizedException.class)
    public void test_getInvoiceById_throwsUnauthorizedException_givenNonOwnerNonAdminUser() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.USER);

        User owner = new User();
        owner.setUserId(2L);
        owner.setRole(Role.USER);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setUser(owner);

        when(invoiceRepository.findById(any(Long.class))).thenReturn(Optional.of(invoice));

        // Act
        sut.getInvoiceById(1L, requester);
    }

    @Test(expected = InvoiceNotFoundException.class)
    public void test_getInvoiceById_throwsInvoiceNotFoundException_givenInvalidId() {
        // Arrange
        User requester = new User();
        requester.setUserId(1L);
        requester.setRole(Role.ADMIN);

        when(invoiceRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act
        sut.getInvoiceById(1L, requester);
    }

    @Test
    public void test_updateInvoice_updatesAndReturnsInvoice_givenValidUserAndInvoice() {
        // Arrange
        User admin = new User();
        admin.setUserId(1L);
        admin.setRole(Role.ADMIN);

        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(1L);
        existingInvoice.setStatus(PaymentStatus.UNPAID);

        Invoice updatedInvoice = new Invoice();
        updatedInvoice.setId(1L);
        updatedInvoice.setStreet("New Street");
        updatedInvoice.setStatus(PaymentStatus.PAID);

        when(invoiceRepository.findById(any(Long.class))).thenReturn(Optional.of(existingInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(updatedInvoice);

        // Act
        Invoice actualInvoice = sut.updateInvoice(admin, updatedInvoice);

        // Assert
        assertEquals(updatedInvoice, actualInvoice);
        verify(invoiceRepository).save(updatedInvoice);
    }

    @Test(expected = UnauthorizedException.class)
    public void test_updateInvoice_throwsUnauthorizedException_givenNonAdminUser() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setRole(Role.USER);

        Invoice updatedInvoice = new Invoice();
        updatedInvoice.setId(1L);

        // Act
        sut.updateInvoice(user, updatedInvoice);
    }

    @Test(expected = InvoiceNotFoundException.class)
    public void test_updateInvoice_throwsInvoiceNotFoundException_givenInvalidInvoiceId() {
        // Arrange
        User admin = new User();
        admin.setUserId(1L);
        admin.setRole(Role.ADMIN);

        Invoice updatedInvoice = new Invoice();
        updatedInvoice.setId(1L);

        when(invoiceRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act
        sut.updateInvoice(admin, updatedInvoice);
    }

    @Test(expected = InvalidInvoiceException.class)
    public void test_updateInvoice_throwsInvalidInvoiceException_givenPaidInvoice() {
        // Arrange
        User admin = new User();
        admin.setUserId(1L);
        admin.setRole(Role.ADMIN);

        Invoice existingInvoice = new Invoice();
        existingInvoice.setId(1L);
        existingInvoice.setStatus(PaymentStatus.PAID);

        Invoice updatedInvoice = new Invoice();
        updatedInvoice.setId(1L);

        when(invoiceRepository.findById(any(Long.class))).thenReturn(Optional.of(existingInvoice));

        // Act
        sut.updateInvoice(admin, updatedInvoice);
    }

    @Test
    public void test_sendPaymentLink_sendsEmail_givenValidUserAndInvoice() {
        // Arrange
        User admin = new User();
        admin.setUserId(1L);
        admin.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setClientEmail("test@example.com");
        invoice.setItems(Arrays.asList(new InvoiceItem()));

        String expectedPaymentLink = "https://payment.example.com";

        when(paymentLinkGenerator.generatePaymentLink(any(Invoice.class), anyString(), any(Order.class))).thenReturn(expectedPaymentLink);

        // Act
        sut.sendPaymentLink(invoice, admin);

        // Assert
        verify(emailService).sendEmail(invoice.getClientEmail(), "Your Invoice Payment Link", "Here is your payment link: " + expectedPaymentLink);
    }

    @Test(expected = UnauthorizedException.class)
    public void test_sendPaymentLink_throwsUnauthorizedException_givenNonAdminUser() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setRole(Role.USER);

        Invoice invoice = new Invoice();
        invoice.setId(1L);

        // Act
        sut.sendPaymentLink(invoice, user);
    }

    @Test
    public void test_sendInvoice_sendsEmail_givenValidUserAndInvoice() {
        // Arrange
        User admin = new User();
        admin.setUserId(1L);
        admin.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setStatus(PaymentStatus.PAID);

        String email = "test@example.com";

        // Act
        sut.sendInvoice(invoice, email, admin);

        // Assert
        verify(emailService).sendEmail(email, "Your Invoice", "Here is your invoice: ...");
    }

    @Test(expected = UnauthorizedException.class)
    public void test_sendInvoice_throwsUnauthorizedException_givenNonAdminUser() {
        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setRole(Role.USER);

        Invoice invoice = new Invoice();
        invoice.setId(1L);

        String email = "test@example.com";

        // Act
        sut.sendInvoice(invoice, email, user);
    }

    @Test(expected = InvalidInvoiceException.class)
    public void test_sendInvoice_throwsInvalidInvoiceException_givenUnpaidInvoice() {
        // Arrange
        User admin = new User();
        admin.setUserId(1L);
        admin.setRole(Role.ADMIN);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setStatus(PaymentStatus.UNPAID);

        String email = "test@example.com";

        // Act
        sut.sendInvoice(invoice, email, admin);
    }

    @Test
    public void test_handleWebhook_completesPayment_givenValidPayload() throws IOException, ApiException {
        // Arrange
        String payload = "{"
                + "\"type\": \"payment.updated\","
                + "\"data\": {"
                + "    \"object\": {"
                + "        \"payment\": {"
                + "            \"order_id\": \"order1\""
                + "        }"
                + "    }"
                + "}"
                + "}";

        // Mock the OrdersApi
        OrdersApi ordersApi = mock(OrdersApi.class);

        // Mock the SquareClientWrapper
        SquareClientWrapper squareClientWrapper = mock(SquareClientWrapper.class);
        when(squareClientWrapper.getOrdersApi()).thenReturn(ordersApi);

        // Create an Order object
        Order order = new Order.Builder("location1")
                .referenceId("order1")
                .build();

        // Create a BatchRetrieveOrdersResponse
        BatchRetrieveOrdersResponse response = new BatchRetrieveOrdersResponse.Builder()
                .orders(Collections.singletonList(order))
                .errors(new ArrayList<>())  // No errors
                .build();

        when(ordersApi.batchRetrieveOrders(any(BatchRetrieveOrdersRequest.class))).thenReturn(response);

        // Set up your InvoiceRepository
        when(invoiceRepository.findByOrderId(anyString())).thenReturn(new Invoice());

        // Inject the mocked SquareClientWrapper into your service
        InvoiceServiceImpl invoiceService = new InvoiceServiceImpl(invoiceRepository, userRepository, emailService, paymentLinkGenerator, squareClientWrapper);

        // Act
        invoiceService.handleWebhook(payload);

        // Assert
        verify(invoiceRepository).findByOrderId(anyString());
        verify(ordersApi).batchRetrieveOrders(any(BatchRetrieveOrdersRequest.class));
    }


}
