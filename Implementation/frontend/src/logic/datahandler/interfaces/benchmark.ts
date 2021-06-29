/**
 * Encapsulates information regarding one benchmark result
 */
import {BenchmarkType} from "./benchmark-type";
import {Summary} from "./summary";
import {Commit} from "./commit";

export interface Benchmark {

  /**
   * Corresponding commit of a benchmark
   */
  get commit(): Commit;

  /**
   * The Device this benchmark was run on
   */
  get device(): string;

  /**
   * The Summary of this benchmark
   */
  get summary(): Summary;

  /**
   * The type of this benchmark
   */
  get type(): BenchmarkType;
}
