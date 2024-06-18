package io.onedev.server.web.component.iteration.choice;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.Iteration;
import io.onedev.server.util.Similarities;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class IterationChoiceProvider extends AbstractIterationChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private final IModel<Collection<Iteration>> choicesModel;
	
	public IterationChoiceProvider(IModel<Collection<Iteration>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void query(String term, int page, Response<Iteration> response) {
		List<Iteration> iterations = new Similarities<Iteration>(choicesModel.getObject()) {

			private static final long serialVersionUID = 1L;

			@Override
			public double getSimilarScore(Iteration object) {
				return Similarities.getSimilarScore(object.getName(), term);
			}
			
		};
		new ResponseFiller<Iteration>(response).fill(iterations, page, WebConstants.PAGE_SIZE);
	}

}