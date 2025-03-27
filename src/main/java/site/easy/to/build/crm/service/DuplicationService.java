package site.easy.to.build.crm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.*;
import site.easy.to.build.crm.util.EmailTokenUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DuplicationService {

    private final CustomerRepository customerRepository;
    private final CustomerLoginInfoRepository customerLoginInfoRepository;
    private final TicketRepository ticketRepository;
    private final LeadRepository leadRepository;
    private final ExpenseRepository expenseRepository;
    private final FileRepository fileRepository;
    private final GoogleDriveFileRepository googleDriveFileRepository;
    private final LeadActionRepository leadActionRepository;

    @Transactional
    public void duplicate(Customer original) {
        Customer clone = cloneCustomer(original);
        customerRepository.save(clone);

        CustomerLoginInfo cliClone = cloneCustomerLoginInfo(original, clone);
        customerLoginInfoRepository.save(cliClone);

        clone.setCustomerLoginInfo(cliClone);
        customerRepository.save(clone);

        cloneLeads(original, clone);
        System.out.println("DUPLICATED LEADS");
        cloneTickets(original, clone);
        System.out.println("DUPLICATED TICKETS");
    }

    private void cloneLeads(Customer original, Customer clone) {
        int customerId = original.getCustomerId();
        List<Lead> leads = leadRepository.findByCustomerCustomerId(customerId);

        for (Lead leadOrg : leads) {
            Lead leadClone = new Lead();
            leadClone.setName(leadOrg.getName());
            leadClone.setStatus(leadOrg.getStatus());
            leadClone.setPhone(leadOrg.getPhone());
            leadClone.setMeetingId(leadOrg.getMeetingId());
            leadClone.setGoogleDrive(leadOrg.getGoogleDrive());
            leadClone.setGoogleDriveFolderId(leadOrg.getGoogleDriveFolderId());

            leadClone.setCustomer(clone);
            leadRepository.save(leadClone);

//            leadClone.setLeadActions(leadOrg.getLeadActions());
            List<LeadAction> leadActions = new ArrayList<>();
            for (LeadAction leadAction : leadOrg.getLeadActions()) {
                LeadAction leadActionClone = new LeadAction();
                leadActionClone.setAction(leadAction.getAction());
                leadActionClone.setTimestamp(leadAction.getTimestamp());

                leadActionClone.setLead(leadClone);

                leadActions.add(leadActionClone);
            }
            leadActionRepository.saveAll(leadActions);
            leadClone.setLeadActions(leadActions);

//            leadClone.setFiles(leadOrg.getFiles());
            List<File> fileClones = new ArrayList<>();
            for (File fileOrg : leadOrg.getFiles()) {
                File fileClone = new File();
                fileClone.setFileName("copy_" + fileOrg.getFileName());
                fileClone.setFileData(fileOrg.getFileData());
                fileClone.setFileType(fileOrg.getFileType());
                fileClone.setContract(fileOrg.getContract());

                fileClone.setLead(leadClone);

                fileClones.add(fileClone);
            }
            fileRepository.saveAll(fileClones);
            leadClone.setFiles(fileClones);

//            leadClone.setGoogleDriveFiles(leadOrg.getGoogleDriveFiles());
            List<GoogleDriveFile> gdFileClones = new ArrayList<>();
            for (GoogleDriveFile gdFileOrg : leadOrg.getGoogleDriveFiles()) {
                GoogleDriveFile gdFileClone = new GoogleDriveFile();
                gdFileClone.setDriveFileId(gdFileOrg.getDriveFileId());
                gdFileClone.setDriveFolderId(gdFileOrg.getDriveFolderId());
                gdFileClone.setContract(gdFileOrg.getContract());

                gdFileClone.setLead(leadClone);

                gdFileClones.add(gdFileClone);
            }
            googleDriveFileRepository.saveAll(gdFileClones);
            leadClone.setGoogleDriveFiles(gdFileClones);

            leadClone.setManager(leadOrg.getManager());
            leadClone.setEmployee(leadOrg.getEmployee());
            leadClone.setCreatedAt(leadOrg.getCreatedAt());

            leadClone.setCustomer(clone);
            leadRepository.save(leadClone);

            // exp
            Expense expOriginal = expenseRepository.findByLeadId(leadOrg.getLeadId());
            Expense expClone = new Expense();
            expClone.setAmount(expOriginal.getAmount());
            expClone.setCreationDate(expOriginal.getCreationDate());
            expClone.setLead(leadClone);
            expenseRepository.save(expClone);

            leadClone.setExpense(expClone);
            leadRepository.save(leadClone);
        }
    }

    private void cloneTickets(Customer original, Customer clone) {
        int customerId = original.getCustomerId();
        List<Ticket> tickets = ticketRepository.findByCustomerCustomerId(customerId);

        for (Ticket ticketOrg : tickets) {
            Ticket ticketClone = new Ticket();
            ticketClone.setSubject(ticketOrg.getSubject());
            ticketClone.setDescription(ticketOrg.getDescription());
            ticketClone.setStatus(ticketOrg.getStatus());
            ticketClone.setPriority(ticketOrg.getPriority());
            ticketClone.setManager(ticketOrg.getManager());
            ticketClone.setEmployee(ticketOrg.getEmployee());
            ticketClone.setCreatedAt(ticketOrg.getCreatedAt());

            ticketClone.setCustomer(clone);
            ticketRepository.save(ticketClone);

            // exp
            Expense expOriginal = expenseRepository.findByTicketId(ticketOrg.getTicketId());
            Expense expClone = new Expense();
            expClone.setAmount(expOriginal.getAmount());
            expClone.setCreationDate(expOriginal.getCreationDate());
            expClone.setTicket(ticketClone);
            expenseRepository.save(expClone);

            ticketClone.setExpense(expClone);
            ticketRepository.save(ticketClone);
        }
    }

    private CustomerLoginInfo cloneCustomerLoginInfo(Customer customerOrg, Customer cloneCustomer) {
        CustomerLoginInfo cliOrg = customerLoginInfoRepository.findByCustomer(customerOrg);

        CustomerLoginInfo cliClone = new CustomerLoginInfo();
        cliClone.setUsername(cloneCustomer.getUser().getUsername());
        cliClone.setPassword(cliOrg.getPassword());
        cliClone.setToken(EmailTokenUtils.generateToken());
        cliClone.setPasswordSet(cliOrg.isPasswordSet());
        cliClone.setCustomer(cloneCustomer);

        return cliClone;
    }

    private Customer cloneCustomer(Customer original) {
        Customer clone = new Customer();
        clone.setName("copy_" + original.getName());
        clone.setEmail("copy_" + original.getEmail());
        clone.setPosition(original.getPosition());
        clone.setPhone(original.getPhone());
        clone.setAddress(original.getAddress());
        clone.setCity(original.getCity());
        clone.setState(original.getState());
        clone.setCountry(original.getCountry());
        clone.setDescription(original.getDescription());
        clone.setTwitter(original.getTwitter());
        clone.setFacebook(original.getFacebook());
        clone.setYoutube(original.getYoutube());
        clone.setUser(original.getUser());
        clone.setCreatedAt(original.getCreatedAt());

        return clone;
    }
}
