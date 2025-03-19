package com.rekindled.embers.recipe;

import java.util.ArrayList;

import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rekindled.embers.RegistryManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class AlchemyRecipe extends AlchemyRecipeBase {

	public static final Serializer SERIALIZER = new Serializer();

	public AlchemyRecipe(ResourceLocation id, Ingredient tablet, ArrayList<Ingredient> aspects, ArrayList<ReagentInfo> reagents, ArrayList<Ingredient> inputs, ItemStack output, ItemStack failure) {
		super(id, tablet, aspects, reagents, inputs, output, failure);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	public static class Serializer implements RecipeSerializer<AlchemyRecipe> {

		@Override
		public AlchemyRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			Ingredient tablet = Ingredient.fromJson(json.get("tablet"));

			ArrayList<Ingredient> inputs = new ArrayList<>();
			JsonArray inputJson = GsonHelper.getAsJsonArray(json, "inputs", null);
			if (inputJson != null) {
				for (JsonElement element : inputJson) {
					inputs.add(Ingredient.fromJson(element));
				}
			}
			ArrayList<Ingredient> aspects = new ArrayList<>();
			JsonArray aspectJson = GsonHelper.getAsJsonArray(json, "aspects", null);
			if (aspectJson != null) {
				for (JsonElement element : aspectJson) {
					aspects.add(Ingredient.fromJson(element));
				}
			}
			ArrayList<ReagentInfo> reagents = new ArrayList<>();
			JsonArray reagentJson = GsonHelper.getAsJsonArray(json, "reagents", null);
			if (reagentJson != null) {
				for (JsonElement element : reagentJson) {
					reagents.add(ReagentInfo.fromJson(element));
				}
			}
			ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
			ItemStack failure;
			if (json.has("failure")) {
				failure = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "failure"));
			} else {
				failure = new ItemStack(RegistryManager.ALCHEMICAL_WASTE.get());
			}
			return new AlchemyRecipe(recipeId, tablet, aspects, reagents, inputs, output, failure);
		}

		@Override
		public @Nullable AlchemyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient tablet = Ingredient.fromNetwork(buffer);
			ArrayList<Ingredient> aspects = buffer.readCollection((i) -> new ArrayList<>(), (buf) -> Ingredient.fromNetwork(buf));
			ArrayList<ReagentInfo> reagents = buffer.readCollection((i) -> new ArrayList<>(), (buf) -> new ReagentInfo(BuiltInRegistries.ITEM.get(buf.readResourceLocation()), buf.readInt(), buf.readInt()));
			ArrayList<Ingredient> inputs = buffer.readCollection((i) -> new ArrayList<>(), (buf) -> Ingredient.fromNetwork(buf));
			ItemStack output = buffer.readItem();
			ItemStack failure = buffer.readItem();

			return new AlchemyRecipe(recipeId, tablet, aspects, reagents, inputs, output, failure);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, AlchemyRecipe recipe) {
			recipe.tablet.toNetwork(buffer);
			buffer.writeCollection(recipe.aspects, (buf, input) -> input.toNetwork(buf));
			buffer.writeCollection(recipe.reagents, (buf, input) -> {
				buf.writeInt(input.max());
				buf.writeInt(input.min());
				buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(input.reagent()));
			});
			buffer.writeCollection(recipe.inputs, (buf, input) -> input.toNetwork(buf));
			buffer.writeItemStack(recipe.output, false);
			buffer.writeItemStack(recipe.failure, false);
		}
	}
}
