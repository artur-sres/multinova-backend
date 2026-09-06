package com.artur_sres.multinova.service;

import com.artur_sres.multinova.dto.cash_exit.*;
import com.artur_sres.multinova.repository.CashExitRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CashExitService {
    private final CashExitRepository entries;
    private final AuthenticatedUserProvider auth;
    public CashExitService(CashExitRepository entries, AuthenticatedUserProvider auth) {
        this.entries = entries;
        this.auth = auth;
    }
    @Transactional
    public CashExitResponseDTO create(CashExitRequestDTO dto) { return entries.create(auth.getCurrentUser().getId(), dto); }
    public List<CashExitResponseDTO> listRecent() { return entries.listRecent(auth.getCurrentUser().getId()); }
}
