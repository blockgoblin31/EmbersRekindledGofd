package com.rekindled.embers.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.api.misc.AlchemyResult;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public interface IAlchemyRecipe extends Recipe<AlchemyContext> {

	public AlchemyCode getCode(long seed);

	public boolean matchesCorrect(AlchemyContext context, Level pLevel);

	public AlchemyResult getResult(AlchemyContext context);

	@Override
	public default ItemStack getToastSymbol() {
		return new ItemStack(RegistryManager.ALCHEMY_TABLET_ITEM.get());
	}

	@Override
	public default RecipeType<?> getType() {
		return RegistryManager.ALCHEMY.get();
	}

	@Override
	public default ItemStack getResultItem(RegistryAccess registry) {
		return getResultItem();
	}

	public Ingredient getCenterInput();

	public List<Ingredient> getInputs();

	public List<Ingredient> getAspects();

	public ItemStack getResultItem();

	public ItemStack getfailureItem();

	@Override
	@Deprecated
	public default boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	record PedestalContents(ItemStack aspect, ItemStack input) {}

	class AlchemyCode {
		public ArrayList<Ingredient> aspects = new ArrayList<>();
		public HashMap<Item, Integer> reagentAmounts = new HashMap<>();

		public AlchemyCode(ArrayList<Ingredient> aspectTypes, ArrayList<ReagentInfo> reagentInfo, int inputs, long seed) {
			Random rand = new Random(seed);
			for (int i = 0; i < inputs; i++) {
				aspects.add(aspectTypes.get(rand.nextInt(aspectTypes.size())));
			}
			for (ReagentInfo info : reagentInfo) {
				reagentAmounts.put(info.reagent, rand.nextInt(info.min, info.max));
			}
		}
	}

	record ReagentInfo(Item reagent, int min, int max) {
		public static ReagentInfo fromJson(JsonElement json) {
			if (!json.isJsonObject()) return null;
			JsonObject obj = json.getAsJsonObject();
			return new ReagentInfo(BuiltInRegistries.ITEM.get((new ResourceLocation(obj.get("item").getAsString()))), obj.get("min").getAsInt(), obj.get("max").getAsInt());
		}

		public JsonObject toJson() {
			JsonObject output = new JsonObject();
			output.add("item", new JsonPrimitive(BuiltInRegistries.ITEM.getKey(reagent).toString()));
			output.add("min", new JsonPrimitive(min));
			output.add("max", new JsonPrimitive(max));
			return output;
		}
	}
}
