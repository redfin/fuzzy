package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.FuzzyPreconditions;
import com.redfin.fuzzy.Literal;
import com.redfin.fuzzy.Subcase;
import com.redfin.fuzzy.Subcases;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class CollectionCase<COLLECTION extends Collection<ELEMENT>, ELEMENT> implements Case<COLLECTION> {

	private Case<Integer> sizeCase = Any.integer().inRange(0, 100);
	private Case<ELEMENT> elementsCase = Literal.nil();

	private boolean homogeneousMode;

	protected abstract COLLECTION createCollection(int expectedSize);

	public CollectionCase<COLLECTION, ELEMENT> withSizeOf(Case<Integer> sizeCase) {
		this.sizeCase = FuzzyPreconditions.checkNotNull("sizeCase is required", sizeCase);
		return this;
	}

	public CollectionCase<COLLECTION, ELEMENT> withSize(int size) {
		if(size < 0)
			throw new IllegalArgumentException("CollectionCase's size cannot be less than zero.");

		this.sizeCase = Literal.value(size);
		return this;
	}

	public CollectionCase<COLLECTION, ELEMENT> withElementsOf(Case<ELEMENT> elementsCase) {
		this.elementsCase = FuzzyPreconditions.checkNotNull("elementsCase is required", elementsCase);
		return this;
	}

	public CollectionCase<COLLECTION, ELEMENT> withElementsOf(Supplier<Case<ELEMENT>> caseFunction) {
		this.elementsCase = FuzzyPreconditions.checkNotNull(
			"The caseFunction supplier returned a null case.",
			FuzzyPreconditions.checkNotNull("caseFunction is required", caseFunction).get()
		);
		return this;
	}

	/**
	 * Instructs this case to build collections where each element is created by the same supplier from its elements
	 * case.
	 */
	public CollectionCase<COLLECTION, ELEMENT> withHomogeneousElementSuppliers() {
		homogeneousMode = true;
		return this;
	}

	@Override
	public Set<Subcase<COLLECTION>> getSubcases() {
		if(homogeneousMode) {
			return Subcases.pairwisePermutations(
				sizeCase.getSubcases(),
				elementsCase.getSubcases(),
				(random, size, element) -> {
					if(size == null || size < 0)
						throw new IllegalStateException(String.format(
							"Supplier for collection size returned an illegal value of %s.",
							size
						));

					COLLECTION result = createCollection(size);
					for(int i = 0; i < size; i++) {
						result.add(element);
					}

					return result;
				}
			);
		}
		else {
			List<Subcase<Integer>> sizeSubcases = new ArrayList<>(sizeCase.getSubcases());
			List<Subcase<ELEMENT>> elementSubcases = new ArrayList<>(elementsCase.getSubcases());

			// Return enough functions to cover either all size cases or all supplier cases, whichever is greater.
			// Add some arbitrarily chosen padding to the size cases to account for some subset of them which will be
			// zero.
			AtomicInteger elementSelector = new AtomicInteger(0);
			int supplierCount = Math.max(sizeSubcases.size() + 2, elementSubcases.size());

			Set<Subcase<COLLECTION>> suppliers = new HashSet<>(supplierCount);
			for(int i = 0; i < supplierCount; i++) {
				suppliers.add(new CollectionSupplier<>(
					this::createCollection,
					FuzzyPreconditions.checkNotNull(
						String.format(
							"Collection case's size case of type %s returned an illegal null supplier",
							sizeCase.getClass()
						),
						sizeSubcases.get(i % sizeSubcases.size())
					),
					elementSubcases,
					elementSelector
				));
			}

			return suppliers;
		}
	}

	private static class CollectionSupplier<D extends Collection<U>, U> implements Subcase<D> {

		private final Function<Integer, D> collectionCreator;
		private final Subcase<Integer> sizeSubcase;
		private final List<Subcase<U>> elementSubcases;
		private final AtomicInteger elementSelector;

		private CollectionSupplier(
			Function<Integer, D> collectionCreator,
			Subcase<Integer> sizeSubcase,
			List<Subcase<U>> elementSubcases,
			AtomicInteger elementSelector
		) {
			this.collectionCreator = collectionCreator;
			this.sizeSubcase = sizeSubcase;
			this.elementSubcases = elementSubcases;
			this.elementSelector = elementSelector;
		}

		@Override
		public D generate(Random random) {
			Integer size = sizeSubcase.generate(random);
			if(size == null || size < 0)
				throw new IllegalStateException(String.format(
					"Supplier for collection size returned an illegal value of %s.",
					size
				));

			D result = collectionCreator.apply(size);
			for(int i = 0; i < size; i++) {
				int j = elementSelector.getAndIncrement() % elementSubcases.size();
				result.add(FuzzyPreconditions.checkNotNull(
					"Case for collection elements returned an illegal null supplier.",
					elementSubcases.get(j)
				).generate(random));
			}

			return result;
		}

	}

	public static class ListCase<T> extends CollectionCase<List<T>, T> {
		@Override
		protected List<T> createCollection(int expectedSize) {
			return new ArrayList<>(expectedSize);
		}
	}

	public static class SetCase<T> extends CollectionCase<Set<T>, T> {
		@Override
		protected Set<T> createCollection(int expectedSize) {
			return new HashSet<>(expectedSize);
		}
	}

}
