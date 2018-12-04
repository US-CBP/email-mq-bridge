package com.rws.email.emailtoqueue.configuration;

import org.springframework.integration.mail.SearchTermStrategy;
import org.springframework.stereotype.Component;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;

@Component
public class ReceiverSearchTerms implements SearchTermStrategy {
    @Override
    public SearchTerm generateSearchTerm(Flags flags, Folder folder) {
        return new FlagTerm(new Flags(Flags.Flag.SEEN), false);
    }
}
