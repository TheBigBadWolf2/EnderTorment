package white_blizz.ender_torment.intergration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.util.ResourceLocation;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@JeiPlugin
public final class JEIHandler implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return Ref.MOD.rl.loc("default");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {

	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {

	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {

	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {

	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {

	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {

	}

	@Override
	public void registerAdvanced(IAdvancedRegistration registration) {

	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

	}
}
