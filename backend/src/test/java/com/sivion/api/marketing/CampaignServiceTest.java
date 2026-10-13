package com.sivion.api.marketing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {
    @Mock
    private CampaignRepository repository;

    @InjectMocks
    private CampaignService service;

    @Test
    void createsCampaignWithTenantAndNormalizedValues() {
        Campaign campaign = new Campaign();
        campaign.setName("  Winter launch ");
        campaign.setChannel(" social ");
        campaign.setBudget(new BigDecimal("2500"));
        campaign.setStatus("planned");
        when(repository.save(campaign)).thenReturn(campaign);

        Campaign result = service.create(4L, campaign);

        assertEquals(4L, result.getTenantId());
        assertEquals("Winter launch", result.getName());
        assertEquals("SOCIAL", result.getChannel());
        assertEquals("PLANNED", result.getStatus());
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        Campaign campaign = new Campaign();
        campaign.setName("Launch");
        campaign.setChannel("EMAIL");
        campaign.setStartDate(LocalDate.of(2026, 11, 2));
        campaign.setEndDate(LocalDate.of(2026, 11, 1));

        assertThrows(ResponseStatusException.class, () -> service.create(4L, campaign));
    }

    @Test
    void cannotUpdateCampaignOutsideTenant() {
        when(repository.findByIdAndTenantId(9L, 4L)).thenReturn(Optional.empty());
        Campaign input = new Campaign();
        input.setName("Updated");
        input.setChannel("EMAIL");

        assertThrows(ResponseStatusException.class, () -> service.update(4L, 9L, input));
        verify(repository).findByIdAndTenantId(9L, 4L);
    }
}
