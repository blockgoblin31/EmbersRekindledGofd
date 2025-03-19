package com.rekindled.embers.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.rekindled.embers.api.misc.AlchemyResult;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public abstract class AlchemyRecipeBase implements IAlchemyRecipe {

	public final ResourceLocation id;

	public final Ingredient tablet;
	public final ArrayList<Ingredient> aspects;
	public final ArrayList<ReagentInfo> reagents;
	public final ArrayList<Ingredient> inputs;

	public final ItemStack output;
	public final ItemStack failure;

	public Long cachedSeed = null;
	public AlchemyCode code;

	public AlchemyRecipeBase(ResourceLocation id, Ingredient tablet, ArrayList<Ingredient> aspects, ArrayList<ReagentInfo> reagents, ArrayList<Ingredient> inputs, ItemStack output, ItemStack failure) {
		this.id = id;
		this.tablet = tablet;
		this.aspects = aspects;
		this.reagents = reagents;
		this.inputs = inputs;
		this.output = output;
		this.failure = failure;
	}

	@Override
	public AlchemyCode getCode(long seed) {
		if (cachedSeed == null || cachedSeed != seed) {
			code = new AlchemyCode(aspects, reagents, inputs.size(), seed - id.getPath().hashCode());
		}
		return code;
	}

	@Override
	public boolean matches(AlchemyContext context, Level pLevel) {
		if (!tablet.test(context.tablet) || inputs.size() != context.contents.size())
			return false;

		ArrayList<PedestalContents> remaining = new ArrayList<PedestalContents>(context.contents);
		for (int i = 0; i < inputs.size(); i++) {
			boolean matched = false;
			for (int j = 0; j < remaining.size(); j++) {
				if (inputs.get(i).test(remaining.get(j).input())) {
					matched = true;
					remaining.remove(j);
					break;
				}
			}
			if (!matched)
				return false;
		}

		ArrayList<ItemStack> remainingReagents = new ArrayList<>(context.reagents);
		return reagents.stream().allMatch((r) -> remainingReagents.stream().anyMatch((i) -> i.is(r.reagent())));
	}

	@Override
	public boolean matchesCorrect(AlchemyContext context, Level pLevel) {
		getCode(context.seed);
		if (!tablet.test(context.tablet) || code.aspects.size() != context.contents.size())
			return false;

		ArrayList<PedestalContents> remaining = new ArrayList<PedestalContents>(context.contents);
		for (int i = 0; i < inputs.size(); i++) {
			boolean matched = false;
			for (int j = 0; j < remaining.size(); j++) {
				if (code.aspects.get(i).test(remaining.get(j).aspect()) && inputs.get(i).test(remaining.get(j).input())) {
					matched = true;
					remaining.remove(j);
					break;
				}
			}
			if (!matched)
				return false;
		}
		ArrayList<ItemStack> remainingReagents = new ArrayList<>(context.reagents);
		return reagents.stream().allMatch((r) -> remainingReagents.stream().filter((i) -> i.is(r.reagent())).mapToInt(ItemStack::getCount).sum() == code.reagentAmounts.get(r.reagent()));
	}

	@Override
	public ItemStack assemble(AlchemyContext context, RegistryAccess registry) {
		getCode(context.seed);
		int blackPins = 0;
		int whitePins = 0;
		HashMap<Item, Integer> reagents = new HashMap<>();

		ArrayList<Ingredient> remainingCode = new ArrayList<Ingredient>(code.aspects);
		for (int i = 0; i < context.contents.size(); i++) {
			for (int j = 0; j < remainingCode.size(); j++) {
				if (remainingCode.get(j).test(context.contents.get(i).aspect())) {
					whitePins++;
					remainingCode.remove(j);
					break;
				}
			}
		}

		ArrayList<PedestalContents> remaining = new ArrayList<PedestalContents>(context.contents);
		for (int i = 0; i < inputs.size(); i++) {
			for (int j = 0; j < remaining.size(); j++) {
				if (code.aspects.get(i).test(remaining.get(j).aspect()) && inputs.get(i).test(remaining.get(j).input())) {
					blackPins++;
					remaining.remove(j);
					break;
				}
			}
		}
		whitePins -= blackPins;

		for (ItemStack reagent : context.reagents) {
			reagents.put(reagent.getItem(), Mth.abs(reagent.getCount() - code.reagentAmounts.get(reagent.getItem())));
		}

		if (blackPins < code.aspects.size() || reagents.values().stream().anyMatch((i) -> i != 0)) {
			ItemStack waste = failure.copy();
			CompoundTag nbt = new CompoundTag();
			nbt.putInt("blackPins", blackPins);
			nbt.putInt("whitePins", whitePins);

			CompoundTag reagentValues = new CompoundTag();
			reagents.forEach((item, amount) -> {
				reagentValues.put(item.getDefaultInstance().getDisplayName().getString(), IntTag.valueOf(amount));
			});
			nbt.put("reagents", reagentValues);

			ListTag aspectNBT = new ListTag();
			ListTag inputNBT = new ListTag();
			for (PedestalContents contents : context.contents) {
				aspectNBT.add(contents.aspect().serializeNBT());
				inputNBT.add(contents.input().serializeNBT());
			}
			nbt.put("aspects", aspectNBT);
			nbt.put("inputs", inputNBT);

			waste.setTag(nbt);
			return waste;
		}
		return output;
	}

	@Override
	public AlchemyResult getResult(AlchemyContext context) {
		getCode(context.seed);
		int blackPins = 0;
		int whitePins = 0;
		HashMap<Item, Integer> reagents = new HashMap<>();

		ArrayList<Ingredient> remainingCode = new ArrayList<Ingredient>(code.aspects);
		for (int i = 0; i < context.contents.size(); i++) {
			for (int j = 0; j < remainingCode.size(); j++) {
				if (remainingCode.get(j).test(context.contents.get(i).aspect())) {
					whitePins++;
					remainingCode.remove(j);
					break;
				}
			}
		}

		ArrayList<PedestalContents> remaining = new ArrayList<PedestalContents>(context.contents);
		for (int i = 0; i < inputs.size(); i++) {
			for (int j = 0; j < remaining.size(); j++) {
				if (code.aspects.get(i).test(remaining.get(j).aspect()) && inputs.get(i).test(remaining.get(j).input())) {
					blackPins++;
					remaining.remove(j);
					break;
				}
			}
		}
		whitePins -= blackPins;

		//ensure that the ingredient order matches the recipe
		List<PedestalContents> contents = new ArrayList<PedestalContents>(context.contents);
		List<PedestalContents> sortedContents = new ArrayList<PedestalContents>();
		for (Ingredient input : inputs) {
			for (PedestalContents pedestal : contents) {
				if (input.test(pedestal.input())) {
					sortedContents.add(pedestal);
					contents.remove(pedestal);
					break;
				}
			}
		}

		for (ItemStack reagent : context.reagents) {
			reagents.put(reagent.getItem(), Mth.abs(reagent.getCount() - code.reagentAmounts.get(reagent.getItem())));
		}

		return new AlchemyResult(sortedContents, getResultItem(), blackPins, whitePins, reagents); //TODO: failures too?
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public Ingredient getCenterInput() {
		return tablet;
	}

	@Override
	public List<Ingredient> getInputs() {
		return inputs;
	}

	@Override
	public List<Ingredient> getAspects() {
		return aspects;
	}

	@Override
	public ItemStack getResultItem() {
		return output;
	}

	@Override
	public ItemStack getfailureItem() {
		return failure;
	}
}
