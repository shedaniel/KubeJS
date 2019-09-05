package dev.latvian.kubejs.item.ingredient;

import dev.latvian.kubejs.KubeJS;
import dev.latvian.kubejs.item.EmptyItemStackJS;
import dev.latvian.kubejs.item.ItemStackJS;
import jdk.nashorn.api.scripting.JSObject;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author LatvianModder
 */
@FunctionalInterface
public interface IngredientJS
{
	static IngredientJS of(@Nullable Object object)
	{
		if (object instanceof IngredientJS)
		{
			return (IngredientJS) object;
		}
		else if (object instanceof String)
		{
			if (object.toString().startsWith("ore:"))
			{
				return new OreDictionaryIngredientJS(object.toString().substring(4));
			}

			return ItemStackJS.of(KubeJS.appendModId(object.toString()));
		}
		else if (object instanceof JSObject)
		{
			JSObject js = (JSObject) object;

			if (js.isArray())
			{
				MatchAnyIngredientJS list = new MatchAnyIngredientJS();

				for (String key : js.keySet())
				{
					IngredientJS ingredient = of(js.getMember(key));

					if (ingredient != EmptyItemStackJS.INSTANCE)
					{
						list.ingredients.add(ingredient);
					}
				}

				return list.ingredients.isEmpty() ? EmptyItemStackJS.INSTANCE : list;
			}
		}

		return ItemStackJS.of(object);
	}

	boolean test(ItemStackJS stack);

	default boolean isEmpty()
	{
		return false;
	}

	default Set<ItemStackJS> getStacks()
	{
		Set<ItemStackJS> set = new LinkedHashSet<>();

		for (ItemStackJS stack : ItemStackJS.list())
		{
			if (test(stack))
			{
				set.add(stack);
			}
		}

		return set;
	}

	default IngredientJS filter(IngredientJS filter)
	{
		return new FilteredIngredientJS(this, filter);
	}

	default IngredientJS not()
	{
		return new NotIngredientJS(this);
	}

	default ItemStackJS getFirst()
	{
		for (ItemStackJS stack : getStacks())
		{
			if (!stack.isEmpty())
			{
				return stack;
			}
		}

		return EmptyItemStackJS.INSTANCE;
	}
}