package com.redfin.fuzzy.cases;

import com.redfin.fuzzy.Any;
import com.redfin.fuzzy.Case;
import com.redfin.fuzzy.Literal;
import com.redfin.fuzzy.FuzzyPreconditions;
import com.redfin.fuzzy.Suppliers;
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
	public Set<Function<Random, COLLECTION>> getSuppliers() {
		if(homogeneousMode) {
			return Suppliers.pairwisePermutations(
				sizeCase.getSuppliers(),
				elementsCase.getSuppliers(),
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
			List<Function<Random, Integer>> sizeSuppliers = new ArrayList<>(sizeCase.getSuppliers());
			List<Function<Random, ELEMENT>> elementSuppliers = new ArrayList<>(elementsCase.getSuppliers());

			// Return enough functions to cover either all size cases or all supplier cases, whichever is greater.
			// Add some arbitrarily chosen padding to the size cases to account for some subset of them which will be
			// zero.
			AtomicInteger elementSelector = new AtomicInteger(0);
			int supplierCount = Math.max(sizeSuppliers.size() + 2, elementSuppliers.size());

			Set<Function<Random, COLLECTION>> suppliers = new HashSet<>(supplierCount);
			for(int i = 0; i < supplierCount; i++) {
				suppliers.add(new CollectionSupplier<>(
					this::createCollection,
					FuzzyPreconditions.checkNotNull(
						String.format(
							"Collection case's size case of type %s returned an illegal null supplier",
							sizeCase.getClass()
						),
						sizeSuppliers.get(i % sizeSuppliers.size())
					),
					elementSuppliers,
					elementSelector
				));
			}

			return suppliers;
		}
	}

	private static class CollectionSupplier<D extends Collection<U>, U> implements Function<Random, D> {

		private final Function<Integer, D> collectionCreator;
		private final Function<Random, Integer> sizeSupplier;
		private final List<Function<Random, U>> elementSuppliers;
		private final AtomicInteger elementSelector;

		private CollectionSupplier(
			Function<Integer, D> collectionCreator,
			Function<Random, Integer> sizeSupplier,
			List<Function<Random, U>> elementSuppliers,
			AtomicInteger elementSelector
		) {
			this.collectionCreator = collectionCreator;
			this.sizeSupplier = sizeSupplier;
			this.elementSuppliers = elementSuppliers;
			this.elementSelector = elementSelector;
		}

		@Override
		public D apply(Random random) {
			Integer size = sizeSupplier.apply(random);
			if(size == null || size < 0)
				throw new IllegalStateException(String.format(
					"Supplier for collection size returned an illegal value of %s.",
					size
				));

			D result = collectionCreator.apply(size);
			for(int i = 0; i < size; i++) {
				int j = elementSelector.getAndIncrement() % elementSuppliers.size();
				result.add(FuzzyPreconditions.checkNotNull(
					"Case for collection elements returned an illegal null supplier.",
					elementSuppliers.get(j)
				).apply(random));
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
