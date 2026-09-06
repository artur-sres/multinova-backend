package com.artur_sres.multinova.service;

import com.artur_sres.multinova.dto.cash_entry.*;
import com.artur_sres.multinova.repository.CashEntryRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CashEntryService {
    private final CashEntryRepository entries;
    private final AuthenticatedUserProvider auth;
    public CashEntryService(CashEntryRepository entries, AuthenticatedUserProvider auth) {
        this.entries = entries;
        this.auth = auth;
    }
    @Transactional
    public CashEntryResponseDTO create(CashEntryRequestDTO dto) { return entries.create(auth.getCurrentUser().getId(), dto); }
    public List<CashEntryResponseDTO> listRecent() { return entries.listRecent(auth.getCurrentUser().getId()); }
}
