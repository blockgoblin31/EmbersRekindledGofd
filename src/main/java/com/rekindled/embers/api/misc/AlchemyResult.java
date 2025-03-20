package com.rekindled.embers.api.misc;

import java.util.HashMap;
import java.util.List;

import com.rekindled.embers.recipe.IAlchemyRecipe.PedestalContents;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AlchemyResult {

	public List<PedestalContents> contents;
	public ItemStack result;
	public int blackPins;
	public int whitePins;
	public HashMap<Item, Integer> reagents;

	public AlchemyResult(List<PedestalContents> contents, ItemStack result, int blackPins, int whitePins, HashMap<Item, Integer> reagents) {
		this.contents = contents;
		this.result = result;
		this.blackPins = blackPins;
		this.whitePins = whitePins;
		this.reagents = reagents;
	}

	public ItemStack createResultStack(ItemStack stack) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("blackPins", blackPins);
		nbt.putInt("whitePins", whitePins);

		nbt.put("result", result.serializeNBT());

		CompoundTag reagentValues = new CompoundTag();
		reagents.forEach((item, amount) -> {
			reagentValues.put(BuiltInRegistries.ITEM.getKey(item).toString(), IntTag.valueOf(amount));
		});
		nbt.put("reagents", reagentValues);

		ListTag aspectNBT = new ListTag();
		ListTag inputNBT = new ListTag();
		for (PedestalContents contents : contents) {
			aspectNBT.add(contents.aspect().serializeNBT());
			inputNBT.add(contents.input().serializeNBT());
		}
		nbt.put("aspects", aspectNBT);
		nbt.put("inputs", inputNBT);

		stack.setTag(nbt);
		return stack;
	}
}
