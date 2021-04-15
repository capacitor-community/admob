import type { PluginListenerHandle } from '@capacitor/core';


type Contra<T> = (x: T) => void
type UnwrapContra<T> = [T] extends [Contra<infer S>] ? S : never
type UnionToIntersection<U> = UnwrapContra<U extends any ? Contra<U> : never>

type Overload<T> =
   (eventName: T, listenerFunc: (...args: any[]) => any) => PluginListenerHandle
type OverloadUnionForEnum<T> = T extends any ? Overload<T> : never

type OverloadUnion<T> = OverloadUnionForEnum<T>
//  Overload<RewardAdPluginEvents.FailedToLoad> | Overload<RewardAdPluginEvents.Dismissed> | Overload<RewardAdPluginEvents.Rewarded>

type Overloads<T> = UnionToIntersection<OverloadUnion<T>>

export type ValidateAllEventsEnumAreImplemented<TEventsEnum, TDefinitionInterface extends { addListener: Overloads<TEventsEnum> }> = TDefinitionInterface;
