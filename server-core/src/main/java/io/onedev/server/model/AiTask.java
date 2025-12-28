package io.onedev.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import io.onedev.server.ai.ChatToolAware;

@Entity
public class AiTask extends AbstractEntity {

    public static final int MAX_INSTRUCTION_LEN = 100000;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User ai;

	@Column(nullable=false, length=MAX_INSTRUCTION_LEN)
	private String instruction;

	@Lob
    @Column(nullable=false, length=65535)
    private ChatToolAware environment;

    public User getAi() {
        return ai;
    }

    public void setAi(User ai) {
        this.ai = ai;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public ChatToolAware getEnvironment() {
        return environment;
    }

    public void setEnvironment(ChatToolAware environment) {
        this.environment = environment;
    }
    
}
