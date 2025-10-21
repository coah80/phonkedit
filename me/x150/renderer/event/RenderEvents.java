package me.x150.renderer.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.class_332;
import net.minecraft.class_4587;

public class RenderEvents {
	public static final Event<RenderEvent<class_4587>> WORLD = create();
	public static final Event<RenderEvent<class_332>> HUD = create();

	private static <T> Event<RenderEvent<T>> create() {
		return EventFactory.createArrayBacked(RenderEvent.class, listeners -> element -> {
			for (RenderEvent<T> listener : listeners) {
				listener.rendered(element);
			}
		});
	}

	@FunctionalInterface
	public interface RenderEvent<T> {
		void rendered(T matrixStack);
	}
}
