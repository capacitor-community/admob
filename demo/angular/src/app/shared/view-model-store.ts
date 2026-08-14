export abstract class ViewModelStore<T> {
  constructor(protected readonly host: T) {}
}
